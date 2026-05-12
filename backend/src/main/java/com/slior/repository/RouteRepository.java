package com.slior.repository;

import com.slior.model.Route;
import com.slior.model.enums.RouteStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    @EntityGraph(attributePaths = {"stops", "repartidor"})
    List<Route> findByRepartidorIdAndIsDeletedFalse(UUID userId);

    List<Route> findByRepartidorIdAndStatusAndIsDeletedFalse(UUID userId, RouteStatus status);

    List<Route> findByFechaPlanificadaAndIsDeletedFalse(LocalDate fecha);

    @EntityGraph(attributePaths = {"stops", "repartidor"})
    Optional<Route> findByIdAndIsDeletedFalse(UUID id);
}
