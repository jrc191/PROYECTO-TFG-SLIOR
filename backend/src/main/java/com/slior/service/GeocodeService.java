package com.slior.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slior.dto.geocode.AddressSuggestionResponse;
import com.slior.model.GeocodeCacheEntry;
import com.slior.repository.GeocodeCacheRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class GeocodeService {

    private static final long RETRY_BACKOFF_MS = 1200;

    private final ObjectMapper objectMapper;
    private final GeocodeCacheRepository geocodeCacheRepository;
    private final com.slior.repository.DireccionRepository direccionRepository;
    private static final Logger log = LoggerFactory.getLogger(GeocodeService.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong lastExternalCallMs = new AtomicLong(0);

    @Value("${geocoder.provider:photon}")
    private String geocoderProvider;

    @Value("${geocoder.photon.url:http://localhost:2322/api}")
    private String photonUrl;

    @Value("${geocoder.nominatim.url:https://nominatim.openstreetmap.org/search}")
    private String nominatimUrl;

    @Value("${geocoder.cache.ttl-ms:43200000}") // 12h
    private long cacheTtlMs;

    @Value("${geocoder.throttle.ms:1000}")
    private long throttleMs;

    public List<AddressSuggestionResponse> searchAddresses(String query) {
        String normalized = normalizeQuery(query);
        if (normalized.length() < 3) return Collections.emptyList();
        String city = extractCityFromQuery(normalized);
        boolean hasCity = !city.isBlank();

        String key = normalized.toLowerCase();
        CacheEntry cached = cache.get(key);
        if (cached != null && !cached.isExpired()) {
            List<AddressSuggestionResponse> cachedResults = keepCityMatches(prioritizeByCity(normalized, cached.results()), city, hasCity);
            if (!cachedResults.isEmpty() || !hasCity) {
                return cachedResults;
            }
        }

        List<AddressSuggestionResponse> persistent = loadFromPersistentCache(key);
        if (!persistent.isEmpty()) {
            List<AddressSuggestionResponse> persistentResults = keepCityMatches(prioritizeByCity(normalized, persistent), city, hasCity);
            if (!persistentResults.isEmpty() || !hasCity) {
                cache.put(key, new CacheEntry(persistentResults, System.currentTimeMillis() + cacheTtlMs));
                return persistentResults;
            }
        }

        List<AddressSuggestionResponse> local = fetchFromLocalDatabase(normalized);
        if (!local.isEmpty()) {
            List<AddressSuggestionResponse> localResults = keepCityMatches(prioritizeByCity(normalized, local), city, hasCity);
            if (!localResults.isEmpty() || !hasCity) {
                cache.put(key, new CacheEntry(localResults, System.currentTimeMillis() + cacheTtlMs));
                saveToPersistentCache(key, localResults);
                return localResults;
            }
        }

        List<AddressSuggestionResponse> results = fetchWithRetries(normalized);
        List<AddressSuggestionResponse> prioritizedResults = keepCityMatches(prioritizeByCity(normalized, results), city, hasCity);
        cache.put(key, new CacheEntry(prioritizedResults, System.currentTimeMillis() + cacheTtlMs));
        saveToPersistentCache(key, prioritizedResults);
        return prioritizedResults;
    }

    public AddressSuggestionResponse reverseGeocode(double lat, double lon) {
        RestTemplate restTemplate = new RestTemplate(createRequestFactory());

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "SliorBackend/1.0 (student project)");
        headers.set("Accept-Language", "es");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(nominatimUrl.replace("/search", "/reverse"))
                .queryParam("format", "json")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("addressdetails", 1)
                .queryParam("email", "slior.student@example.com");

        String url = builder.toUriString();

        try {
            // Eliminamos el throttle temporalmente para evitar los timeouts de 20s
            // applyThrottle(); 
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode node = objectMapper.readTree(response.getBody());
                String displayName = node.path("display_name").asText(null);
                if (displayName != null) {
                    return new AddressSuggestionResponse(displayName, lat, lon);
                }
            }
        } catch (Exception e) {
            log.warn("Reverse geocode error for lat={}, lon={}, errorType={}",
                    lat, lon, e.getClass().getSimpleName());
        }

        return new AddressSuggestionResponse("Dirección desconocida", lat, lon);
    }

    private List<AddressSuggestionResponse> fetchFromLocalDatabase(String query) {
        log.info("Searching in local database for: {}", query);
        try {
            return direccionRepository.searchByFuzzyName(query, org.springframework.data.domain.PageRequest.of(0, 10))
                    .stream()
                    .map(d -> {
                        String displayName = d.getNombre();
                        if (d.getNumero() != null && !d.getNumero().isBlank()) displayName += ", " + d.getNumero();
                        if (d.getMunicipio() != null && !d.getMunicipio().isBlank()) displayName += ", " + d.getMunicipio();
                        if (d.getProvincia() != null && !d.getProvincia().isBlank()) displayName += ", " + d.getProvincia();
                        if (d.getCodigoPostal() != null && !d.getCodigoPostal().isBlank()) displayName += " (" + d.getCodigoPostal() + ")";
                        
                        return new AddressSuggestionResponse(displayName, d.getLatitud(), d.getLongitud());
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Local database search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<AddressSuggestionResponse> fetchWithRetries(String query) {
        List<AddressSuggestionResponse> primary = geocoderProvider.equalsIgnoreCase("photon")
                ? fetchFromPhoton(query)
                : fetchNominatimFallbacks(query);
        if (!primary.isEmpty()) return primary;

        List<AddressSuggestionResponse> fallback = geocoderProvider.equalsIgnoreCase("photon")
                ? fetchNominatimFallbacks(query)
                : fetchFromPhoton(query);
        return fallback;
    }

    private List<AddressSuggestionResponse> fetchNominatimFallbacks(String query) {
        String city = extractCityFromQuery(query);
        boolean hasCity = !city.isBlank();

        List<AddressSuggestionResponse> structured = keepCityMatches(fetchStructured(query), city, hasCity);
        if (!structured.isEmpty()) return structured;

        if (query.matches("\\d{5}")) {
            List<AddressSuggestionResponse> postal = keepCityMatches(fetchFromNominatim(query, true, true), city, hasCity);
            if (!postal.isEmpty()) return postal;
        }

        List<AddressSuggestionResponse> exact = keepCityMatches(fetchFromNominatim(query, true, false), city, hasCity);
        if (!exact.isEmpty()) return exact;

        List<AddressSuggestionResponse> noCountryFilter = keepCityMatches(fetchFromNominatim(query, false, false), city, hasCity);
        if (!noCountryFilter.isEmpty()) return noCountryFilter;

        if (hasCity) {
            List<AddressSuggestionResponse> relaxedByCity = fetchRelaxedByCity(query, city);
            if (!relaxedByCity.isEmpty()) {
                return relaxedByCity;
            }

            List<AddressSuggestionResponse> tokenMatches = fetchByContainsTokens(query, city);
            if (!tokenMatches.isEmpty()) {
                return tokenMatches;
            }
            return Collections.emptyList();
        }

        String[] tokens = query.split("\\s+");
        for (int i = tokens.length - 1; i >= 1; i--) {
            String broader = String.join(" ", Arrays.copyOf(tokens, i)).trim();
            if (broader.length() < 3) continue;

            List<AddressSuggestionResponse> broaderEs = fetchFromNominatim(broader, true, false);
            if (!broaderEs.isEmpty()) return broaderEs;
        }

        return Collections.emptyList();
    }

    private List<AddressSuggestionResponse> fetchStructured(String query) {
        String[] rawParts = query.split(",");
        List<String> parts = new ArrayList<>();
        for (String part : rawParts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }

        if (parts.size() < 2) return Collections.emptyList();

        String street = parts.get(0);
        String city = parts.get(1);
        String state = parts.size() >= 3 ? parts.get(2) : "";
        String country = parts.size() >= 4 ? parts.get(3) : "";

        if (street.length() < 3 || city.length() < 2) return Collections.emptyList();

        RestTemplate restTemplate = new RestTemplate(createRequestFactory());

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "SliorBackend/1.0 (student project)");
        headers.set("Accept-Language", "es");

        UriComponentsBuilder baseBuilder = UriComponentsBuilder.fromHttpUrl(nominatimUrl)
                .queryParam("format", "json")
                .queryParam("addressdetails", 1)
                .queryParam("limit", 10)
                .queryParam("street", street)
                .queryParam("city", city)
                .queryParam("email", "slior.student@example.com");

        if (!state.isBlank()) {
            baseBuilder.queryParam("state", state);
        }
        if (!country.isBlank()) {
            baseBuilder.queryParam("country", country);
        }
        if (country.equalsIgnoreCase("España") || country.equalsIgnoreCase("Spain")) {
            baseBuilder.queryParam("countrycodes", "es");
        }

        String url = baseBuilder.toUriString();

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                applyThrottle();
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

                HttpStatusCode status = response.getStatusCode();
                if (status.value() == 429 || status.is5xxServerError()) {
                    long waitMs = getRetryAfterMs(response.getHeaders().getFirst("Retry-After"), RETRY_BACKOFF_MS);
                    log.warn("Nominatim structured {} for queryHash={}, attempt {}/2. Waiting {}ms",
                            status.value(), hashForLog(query), attempt, waitMs);
                    if (attempt == 2) {
                        return Collections.emptyList();
                    }
                    safeSleep(waitMs);
                    continue;
                }

                if (!status.is2xxSuccessful() || response.getBody() == null) {
                    return Collections.emptyList();
                }

                return parseAddresses(response.getBody());
            } catch (Exception e) {
                log.warn("Nominatim structured error for queryHash={}, errorType={}",
                        hashForLog(query), e.getClass().getSimpleName());
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<AddressSuggestionResponse> fetchFromNominatim(String query, boolean onlySpain, boolean isPostalCode) {
        RestTemplate restTemplate = new RestTemplate(createRequestFactory());

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "SliorBackend/1.0 (student project)");
        headers.set("Accept-Language", "es");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(nominatimUrl)
                .queryParam("format", "json")
                .queryParam("addressdetails", 1)
                .queryParam("limit", 10)
                .queryParam("email", "slior.student@example.com");

        if (isPostalCode) {
            builder.queryParam("postalcode", query);
            builder.queryParam("countrycodes", "es");
        } else {
            builder.queryParam("q", query);
            if (onlySpain) {
                builder.queryParam("countrycodes", "es");
            }
        }

        String url = builder.toUriString();

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                applyThrottle();
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

                HttpStatusCode status = response.getStatusCode();
                if (status.value() == 429 || status.is5xxServerError()) {
                    long waitMs = getRetryAfterMs(response.getHeaders().getFirst("Retry-After"), RETRY_BACKOFF_MS);
                    log.warn("Nominatim {} for queryHash={}, attempt {}/2. Waiting {}ms",
                            status.value(), hashForLog(query), attempt, waitMs);
                    if (attempt == 2) {
                        return Collections.emptyList();
                    }
                    safeSleep(waitMs);
                    continue;
                }

                if (!status.is2xxSuccessful() || response.getBody() == null) {
                    return Collections.emptyList();
                }

                return parseAddresses(response.getBody());
            } catch (Exception e) {
                log.warn("Nominatim error for queryHash={}, errorType={}",
                        hashForLog(query), e.getClass().getSimpleName());
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<AddressSuggestionResponse> fetchFromPhoton(String query) {
        RestTemplate restTemplate = new RestTemplate(createRequestFactory());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(photonUrl)
                .queryParam("q", query)
                .queryParam("limit", 10);

        String url = builder.toUriString();

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                applyThrottle();
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(new HttpHeaders()),
                        String.class
                );

                HttpStatusCode status = response.getStatusCode();
                if (status.value() == 429 || status.is5xxServerError()) {
                    long waitMs = getRetryAfterMs(response.getHeaders().getFirst("Retry-After"), RETRY_BACKOFF_MS);
                    log.warn("Photon {} for queryHash={}, attempt {}/2. Waiting {}ms",
                            status.value(), hashForLog(query), attempt, waitMs);
                    if (attempt == 2) {
                        return Collections.emptyList();
                    }
                    safeSleep(waitMs);
                    continue;
                }

                if (!status.is2xxSuccessful() || response.getBody() == null) {
                    return Collections.emptyList();
                }

                return parsePhoton(response.getBody());
            } catch (Exception e) {
                log.warn("Photon error for queryHash={}, errorType={}",
                        hashForLog(query), e.getClass().getSimpleName());
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private void applyThrottle() {
        long now = System.currentTimeMillis();
        long last = lastExternalCallMs.get();
        long elapsed = now - last;
        if (elapsed < throttleMs) {
            long wait = throttleMs - elapsed;
            safeSleep(wait);
        }
        lastExternalCallMs.set(System.currentTimeMillis());
    }

    private List<AddressSuggestionResponse> parseAddresses(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (!root.isArray()) return Collections.emptyList();

        List<AddressSuggestionResponse> out = new ArrayList<>();
        for (JsonNode node : root) {
            String displayName = node.path("display_name").asText(null);
            String latStr = node.path("lat").asText(null);
            String lonStr = node.path("lon").asText(null);

            if (displayName == null || latStr == null || lonStr == null) continue;

            Double lat = parseDouble(latStr);
            Double lon = parseDouble(lonStr);
            if (lat == null || lon == null) continue;

            out.add(new AddressSuggestionResponse(displayName, lat, lon));
        }
        return out;
    }

    private List<AddressSuggestionResponse> parsePhoton(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode features = root.path("features");
        if (!features.isArray()) return Collections.emptyList();

        List<AddressSuggestionResponse> out = new ArrayList<>();
        for (JsonNode feature : features) {
            JsonNode properties = feature.path("properties");
            JsonNode geometry = feature.path("geometry");
            JsonNode coords = geometry.path("coordinates");

            if (!coords.isArray() || coords.size() < 2) continue;

            Double lon = parseDouble(coords.get(0).asText(null));
            Double lat = parseDouble(coords.get(1).asText(null));
            if (lat == null || lon == null) continue;

            String name = properties.path("name").asText("");
            String street = properties.path("street").asText("");
            String city = properties.path("city").asText("");
            String state = properties.path("state").asText("");
            String country = properties.path("country").asText("");

            String displayName = joinNonEmpty(name, street, city, state, country);
            if (displayName.isBlank()) {
                displayName = properties.path("label").asText("");
            }
            if (displayName.isBlank()) continue;

            out.add(new AddressSuggestionResponse(displayName, lat, lon));
        }
        return out;
    }

    private SimpleClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(6000);
        return factory;
    }

    private void safeSleep(long waitMs) {
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long getRetryAfterMs(String retryAfter, long defaultMs) {
        if (retryAfter == null || retryAfter.isBlank()) return defaultMs;
        try {
            long seconds = Long.parseLong(retryAfter.trim());
            return Math.max(seconds * 1000, defaultMs);
        } catch (NumberFormatException e) {
            return defaultMs;
        }
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    private List<AddressSuggestionResponse> loadFromPersistentCache(String key) {
        try {
            return geocodeCacheRepository.findByQueryNormalized(key)
                    .map(entry -> {
                        try {
                            // Actualizar último acceso para mantenimiento de caché
                            entry.setLastAccessedAt(java.time.LocalDateTime.now());
                            geocodeCacheRepository.save(entry);
                            
                            return objectMapper.readValue(entry.getResults(), new TypeReference<List<AddressSuggestionResponse>>() {});
                        } catch (Exception e) {
                            return Collections.<AddressSuggestionResponse>emptyList();
                        }
                    })
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.warn("Persistent geocode cache unavailable. Using live query only. errorType={}",
                    e.getClass().getSimpleName());
            return Collections.emptyList();
        }
    }

    private void saveToPersistentCache(String key, List<AddressSuggestionResponse> results) {
        try {
            String payload = objectMapper.writeValueAsString(results);
            GeocodeCacheEntry entry = GeocodeCacheEntry.builder()
                    .queryNormalized(key)
                    .results(payload)
                    .source(geocoderProvider)
                    .lastAccessedAt(java.time.LocalDateTime.now())
                    .build();
            geocodeCacheRepository.save(entry);
        } catch (Exception e) {
            log.warn("Unable to persist geocode cache for queryHash={}, errorType={}",
                    hashForLog(key), e.getClass().getSimpleName());
        }
    }

    private String hashForLog(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return "sha256-unavailable";
        }
    }

    private String joinNonEmpty(String... parts) {
        List<String> clean = new ArrayList<>();
        for (String part : parts) {
            if (part != null) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    clean.add(trimmed);
                }
            }
        }
        return String.join(", ", clean);
    }

    private String normalizeQuery(String raw) {
        if (raw == null) return "";
        return raw
                .replaceAll("\\.\\.+", " ")
                .replaceAll("[^\\p{L}\\p{N},\\s-]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<AddressSuggestionResponse> prioritizeByCity(String query, List<AddressSuggestionResponse> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        String city = extractCityFromQuery(query);
        if (city.isBlank()) {
            return results;
        }

        List<AddressSuggestionResponse> ordered = new ArrayList<>(results);
        ordered.sort(Comparator.comparing(suggestion -> !containsCity(suggestion.displayName(), city)));
        return ordered;
    }

    private String extractCityFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        String[] parts = query.split(",");
        if (parts.length < 2) {
            return "";
        }

        return parts[1].trim();
    }

    private boolean containsCity(String displayName, String city) {
        if (displayName == null || city == null) {
            return false;
        }

        return displayName.toLowerCase(Locale.ROOT).contains(city.toLowerCase(Locale.ROOT));
    }

    private List<AddressSuggestionResponse> keepCityMatches(List<AddressSuggestionResponse> results, String city, boolean hasCity) {
        if (!hasCity || results == null || results.isEmpty()) {
            return results;
        }

        List<AddressSuggestionResponse> filtered = new ArrayList<>();
        for (AddressSuggestionResponse suggestion : results) {
            if (containsCity(suggestion.displayName(), city)) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }

    private List<AddressSuggestionResponse> fetchRelaxedByCity(String query, String city) {
        String street = extractStreetFromQuery(query);
        if (street.isBlank()) {
            return Collections.emptyList();
        }

        String[] tokens = street.split("\\s+");
        for (int i = tokens.length; i >= 1; i--) {
            String relaxedStreet = String.join(" ", Arrays.copyOf(tokens, i)).trim();
            if (relaxedStreet.length() < 3) {
                continue;
            }

            String relaxedQuery = relaxedStreet + ", " + city;
            List<AddressSuggestionResponse> relaxedResults = keepCityMatches(
                    fetchFromNominatim(relaxedQuery, true, false),
                    city,
                    true
            );
            if (!relaxedResults.isEmpty()) {
                return relaxedResults;
            }
        }

        return Collections.emptyList();
    }

    private List<AddressSuggestionResponse> fetchByContainsTokens(String query, String city) {
        String street = extractStreetFromQuery(query);
        if (street.isBlank()) {
            return Collections.emptyList();
        }

        List<String> tokens = extractStreetTokens(street);
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<AddressSuggestionResponse> candidates = new ArrayList<>();
        candidates.addAll(fetchFromNominatim(street + ", " + city, true, false));
        candidates.addAll(fetchFromNominatim(street + " " + city, true, false));
        candidates.addAll(fetchFromNominatim(city, true, false));

        List<AddressSuggestionResponse> cityCandidates = keepCityMatches(removeDuplicates(candidates), city, true);
        if (cityCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<AddressSuggestionResponse> strict = new ArrayList<>();
        List<AddressSuggestionResponse> soft = new ArrayList<>();

        for (AddressSuggestionResponse suggestion : cityCandidates) {
            String text = suggestion.displayName() == null ? "" : suggestion.displayName().toLowerCase(Locale.ROOT);
            long matches = tokens.stream().filter(text::contains).count();
            if (matches == tokens.size()) {
                strict.add(suggestion);
            } else if (matches > 0) {
                soft.add(suggestion);
            }
        }

        if (!strict.isEmpty()) {
            return strict;
        }
        return soft;
    }

    private List<String> extractStreetTokens(String street) {
        if (street == null || street.isBlank()) {
            return Collections.emptyList();
        }

        Set<String> stopWords = Set.of("calle", "avenida", "av", "de", "del", "la", "el", "los", "las");
        List<String> tokens = new ArrayList<>();
        for (String rawToken : street.toLowerCase(Locale.ROOT).split("\\s+")) {
            String token = rawToken.trim();
            if (token.length() < 3) {
                continue;
            }
            if (stopWords.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private List<AddressSuggestionResponse> removeDuplicates(List<AddressSuggestionResponse> items) {
        List<AddressSuggestionResponse> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (AddressSuggestionResponse item : items) {
            String key = item.displayName() + "|" + item.latitude() + "|" + item.longitude();
            if (seen.add(key)) {
                out.add(item);
            }
        }
        return out;
    }

    private String extractStreetFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        String[] parts = query.split(",");
        if (parts.length == 0) {
            return "";
        }

        return parts[0].trim();
    }

    private record CacheEntry(List<AddressSuggestionResponse> results, long expiresAtMs) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMs;
        }
    }
}
