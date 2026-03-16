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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class GeocodeService {

    private static final long RETRY_BACKOFF_MS = 1200;

    private final ObjectMapper objectMapper;
    private final GeocodeCacheRepository geocodeCacheRepository;
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

        String key = normalized.toLowerCase();
        CacheEntry cached = cache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.results();
        }

        List<AddressSuggestionResponse> persistent = loadFromPersistentCache(key);
        if (!persistent.isEmpty()) {
            cache.put(key, new CacheEntry(persistent, System.currentTimeMillis() + cacheTtlMs));
            return persistent;
        }

        List<AddressSuggestionResponse> results = fetchWithRetries(normalized);
        cache.put(key, new CacheEntry(results, System.currentTimeMillis() + cacheTtlMs));
        saveToPersistentCache(key, results);
        return results;
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
        List<AddressSuggestionResponse> structured = fetchStructured(query);
        if (!structured.isEmpty()) return structured;

        if (query.matches("\\d{5}")) {
            List<AddressSuggestionResponse> postal = fetchFromNominatim(query, true, true);
            if (!postal.isEmpty()) return postal;
        }

        List<AddressSuggestionResponse> exact = fetchFromNominatim(query, true, false);
        if (!exact.isEmpty()) return exact;

        List<AddressSuggestionResponse> noCountryFilter = fetchFromNominatim(query, false, false);
        if (!noCountryFilter.isEmpty()) return noCountryFilter;

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
                    log.warn("Nominatim structured {} for '{}', attempt {}/2. Waiting {}ms", status.value(), query, attempt, waitMs);
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
                log.warn("Nominatim structured error for '{}': {}", query, e.getMessage());
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
                    log.warn("Nominatim {} for '{}', attempt {}/2. Waiting {}ms", status.value(), query, attempt, waitMs);
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
                log.warn("Nominatim error for '{}': {}", query, e.getMessage());
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
                    log.warn("Photon {} for '{}', attempt {}/2. Waiting {}ms", status.value(), query, attempt, waitMs);
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
                log.warn("Photon error for '{}': {}", query, e.getMessage());
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
        return geocodeCacheRepository.findById(key)
                .filter(entry -> entry.getExpiresAtMs() > System.currentTimeMillis())
                .map(entry -> {
                    try {
                        return objectMapper.readValue(entry.getPayload(), new TypeReference<List<AddressSuggestionResponse>>() {});
                    } catch (Exception e) {
                        return Collections.<AddressSuggestionResponse>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    private void saveToPersistentCache(String key, List<AddressSuggestionResponse> results) {
        try {
            String payload = objectMapper.writeValueAsString(results);
            GeocodeCacheEntry entry = GeocodeCacheEntry.builder()
                    .query(key)
                    .payload(payload)
                    .expiresAtMs(System.currentTimeMillis() + cacheTtlMs)
                    .build();
            geocodeCacheRepository.save(entry);
        } catch (Exception e) {
            log.warn("Unable to persist geocode cache for '{}': {}", key, e.getMessage());
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

    private record CacheEntry(List<AddressSuggestionResponse> results, long expiresAtMs) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMs;
        }
    }
}
