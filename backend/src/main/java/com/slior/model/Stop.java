package com.slior.model;

import com.slior.model.enums.StopStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stops")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    private String telefonoDestinatario;

    // Coordenadas para el mapa
    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    // Posición en la ruta optimizada (1, 2, 3...)
    @Column(nullable = false)
    private Integer ordenVisita;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StopStatus status;

    @Column
    private String notas;

    // Momento real de entrega (null hasta que se entrega)
    @Column
    private LocalDateTime entregadoEn;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}