package com.slior.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "geocode_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeCacheEntry {

    @Id
    @Column(nullable = false, unique = true)
    private String query;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private long expiresAtMs;
}
