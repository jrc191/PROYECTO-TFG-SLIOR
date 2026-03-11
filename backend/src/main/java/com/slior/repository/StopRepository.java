package com.slior.repository;

import com.slior.model.Stop;
import com.slior.model.enums.StopStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StopRepository extends JpaRepository<Stop, UUID> {

    List<Stop> findByRouteIdAndIsDeletedFalseOrderByOrdenVisitaAsc(UUID routeId);

    List<Stop> findByRouteIdAndStatusAndIsDeletedFalse(UUID routeId, StopStatus status);
}