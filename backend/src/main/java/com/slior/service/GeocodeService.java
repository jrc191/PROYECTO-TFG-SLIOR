package com.slior.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slior.dto.geocode.AddressSuggestionResponse;
import com.slior.model.GeocodeCacheEntry;
import com.slior.repository.GeocodeCacheRepository;
import com.slior.repository.DireccionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GeocodeService {

    private final ObjectMapper objectMapper;
    private final GeocodeCacheRepository geocodeCacheRepository;
    private final DireccionRepository direccionRepository;
    private static final Logger log = LoggerFactory.getLogger(GeocodeService.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Value("${geocoder.cache.ttl-ms:43200000}") // 12h
    private long cacheTtlMs;

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

        List<AddressSuggestionResponse> local = fetchFromLocalDatabase(normalized);
        if (!local.isEmpty()) {
            cache.put(key, new CacheEntry(local, System.currentTimeMillis() + cacheTtlMs));
            saveToPersistentCache(key, local);
            return local;
        }

        return Collections.emptyList();
    }

    public AddressSuggestionResponse reverseGeocode(double lat, double lon) {
        // Al haber eliminado servicios externos (Nominatim/Photon),
        // devolvemos una respuesta genérica basada en coordenadas.
        return new AddressSuggestionResponse(
                String.format("Ubicación en %.5f, %.5f", lat, lon),
                lat,
                lon
        );
    }

    private List<AddressSuggestionResponse> fetchFromLocalDatabase(String query) {
        log.info("Buscando en base de datos local (OSM) para: {}", query);
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
            log.error("Error en búsqueda local: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<AddressSuggestionResponse> loadFromPersistentCache(String key) {
        try {
            return geocodeCacheRepository.findByQueryNormalized(key)
                    .map(entry -> {
                        try {
                            entry.setLastAccessedAt(java.time.LocalDateTime.now());
                            geocodeCacheRepository.save(entry);
                            return objectMapper.readValue(entry.getResults(), new TypeReference<List<AddressSuggestionResponse>>() {});
                        } catch (Exception e) {
                            return Collections.<AddressSuggestionResponse>emptyList();
                        }
                    })
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void saveToPersistentCache(String key, List<AddressSuggestionResponse> results) {
        try {
            String payload = objectMapper.writeValueAsString(results);
            GeocodeCacheEntry entry = GeocodeCacheEntry.builder()
                    .queryNormalized(key)
                    .results(payload)
                    .source("local-db")
                    .lastAccessedAt(java.time.LocalDateTime.now())
                    .build();
            geocodeCacheRepository.save(entry);
        } catch (Exception e) {
            log.warn("No se pudo persistir en caché para: {}", key);
        }
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
