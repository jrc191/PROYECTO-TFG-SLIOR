package com.slior.repository;

import com.slior.model.GeocodeCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para el caché de geocodificación.
 */
@Repository
public interface GeocodeCacheRepository extends JpaRepository<GeocodeCacheEntry, String> {
    Optional<GeocodeCacheEntry> findByQueryNormalized(String queryNormalized);
}

