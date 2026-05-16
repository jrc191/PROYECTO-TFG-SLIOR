package com.slior.dto.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para actualizar una ruta existente.
 */
public record UpdateRouteRequest(
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,

        @NotNull(message = "La fecha no puede estar vacía")
        LocalDate fechaPlanificada,

        String notas,

        @NotEmpty(message = "La ruta debe tener al menos una parada")
        @Valid
        List<StopRequest> paradas
) {}
