package com.slior.model;

import com.slior.model.enums.UserRole;
import com.slior.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un usuario del sistema.
 * Usa UUID como clave primaria y borrado lógico (isDeleted), en lugar de borrado completo.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private VehicleType vehicleType = VehicleType.VAN;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deletion_requested_at")
    private LocalDateTime deletionRequestedAt;

    @Column(name = "tratamiento_limitado", nullable = false)
    @Builder.Default
    private Boolean tratamientoLimitado = false;

    @Column(name = "consentimiento_notificaciones", nullable = false)
    @Builder.Default
    private Boolean consentimientoNotificaciones = false;

    @Column(name = "consentimiento_geolocalizacion", nullable = false)
    @Builder.Default
    private Boolean consentimientoGeolocalizacion = false;

    @Column(name = "anonymized_at")
    private LocalDateTime anonymizedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Excluir contraseña del toString generado por Lombok
    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', rol=" + rol + ", vehicleType=" + vehicleType + "}";
    }
}
