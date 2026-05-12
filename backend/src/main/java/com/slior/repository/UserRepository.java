package com.slior.repository;

import com.slior.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad User.
 * Spring Data genera automáticamente las implementaciones.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Busca usuario por email (solo activos, filtrado por @Where). */
    Optional<User> findByEmail(String email);

    /** Verifica si ya existe un usuario con ese email. */
    boolean existsByEmail(String email);

    /** 
     * Busca usuarios marcados para borrar que han superado el periodo de gracia.
     * Usa Native Query para ignorar el @Where(is_deleted = false).
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM users WHERE is_deleted = true AND anonymized_at IS NULL AND deletion_requested_at < :threshold", nativeQuery = true)
    java.util.List<User> findPendingAnonymization(java.time.LocalDateTime threshold);
}
