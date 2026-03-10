package com.slior.dto.route;

import com.slior.model.Route;
import com.slior.model.enums.RouteStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RouteResponse(
        UUID id,
        String nombre,
        LocalDate fechaPlanificada,
        RouteStatus status,
        UUID repartidorId,
        String repartidorNombre,
        List<StopResponse> paradas,
        Double distanciaTotal,
        Integer tiempoEstimado,
        String notas,
        LocalDateTime createdAt
) {
    public static RouteResponse from(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getNombre(),
                route.getFechaPlanificada(),
                route.getStatus(),
                route.getRepartidor().getId(),
                route.getRepartidor().getNombre(),
                route.getStops().stream()
                        .filter(s -> !s.isDeleted())
                        .map(StopResponse::from)
                        .toList(),
                route.getDistanciaTotal(),
                route.getTiempoEstimado(),
                route.getNotas(),
                route.getCreatedAt()
        );
    }
}