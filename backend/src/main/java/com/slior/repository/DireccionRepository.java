package com.slior.repository;

import com.slior.model.Direccion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para búsqueda de direcciones locales.
 * Utiliza el índice trigram de PostgreSQL para búsquedas difusas.
 */
@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    /**
     * Busca direcciones por nombre usando similitud trigram (PostgreSQL).
     * @param query Texto de búsqueda
     * @param pageable Limitación de resultados
     * @return Lista de direcciones que coinciden
     */
    @Query("SELECT d FROM Direccion d WHERE d.nombre ILIKE %:query% OR d.municipio ILIKE %:query%")
    List<Direccion> searchByFuzzyName(String query, Pageable pageable);
}

