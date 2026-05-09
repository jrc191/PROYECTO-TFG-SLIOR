package com.slior.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "geocode_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeCacheEntry {

    @Id
    @Column(name = "query_normalized", nullable = false)
    private String queryNormalized;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results", columnDefinition = "jsonb")
    private String results;

    @Column(length = 20)
    private String source; // 'local' | 'nominatim'

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
}

