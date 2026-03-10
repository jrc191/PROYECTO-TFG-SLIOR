package com.slior.dto.route;

import jakarta.validation.constraints.NotNull;

public record OptimizeRouteRequest(

        @NotNull(message = "La latitud del punto de inicio es obligatoria")
        Double puntoInicioLat,

        @NotNull(message = "La longitud del punto de inicio es obligatoria")
        Double puntoInicioLon
) {}