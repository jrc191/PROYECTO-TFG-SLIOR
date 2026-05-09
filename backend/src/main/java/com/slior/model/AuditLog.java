package com.slior.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad para el registro de auditoría (RGPD).
 * Almacena acciones críticas realizadas sobre datos personales.
 */
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private UUID userId; // ID del usuario que realiza la acción (null si es anónimo)

    @Column(nullable = false)
    private String action; // Ejemplo: LOGIN, EXPORT_DATA, DELETE_ACCOUNT

    private String entity; // Ejemplo: User, Route, Stop

    @Column(name = "ip_hash")
    private String ipHash; // Hash SHA-256 de la IP para anonimizar pero permitir trazabilidad

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "request_id")
    private String requestId; // Correlation ID (X-Request-ID)
}

