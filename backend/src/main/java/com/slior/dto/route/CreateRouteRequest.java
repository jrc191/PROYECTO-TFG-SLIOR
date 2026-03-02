package com.slior.dto.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateRouteRequest(

        @NotBlank(message = "El nombre de la ruta es obligatorio")
        String nombre,

        @NotNull(message = "La fecha planificada es obligatoria")
        LocalDate fechaPlanificada,

        @NotNull(message = "El repartidor es obligatorio")
        UUID repartidorId,

        @NotEmpty(message = "La ruta debe tener al menos una parada")
        @Valid
        List<StopRequest> paradas,

        String notas
) {}