package com.slior.dto.route;

import com.slior.model.enums.VehicleType;
import jakarta.validation.constraints.NotNull;

public record OptimizeRouteRequest(

        @NotNull(message = "La latitud del punto de inicio es obligatoria")
        Double puntoInicioLat,

        @NotNull(message = "La longitud del punto de inicio es obligatoria")
        Double puntoInicioLon,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        VehicleType vehicleType
) {}