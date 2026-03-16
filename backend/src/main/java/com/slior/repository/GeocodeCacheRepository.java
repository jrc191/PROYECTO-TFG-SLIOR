package com.slior.repository;

import com.slior.model.GeocodeCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeocodeCacheRepository extends JpaRepository<GeocodeCacheEntry, String> {
}
