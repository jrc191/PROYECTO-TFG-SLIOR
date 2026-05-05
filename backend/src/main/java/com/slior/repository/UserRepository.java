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
}