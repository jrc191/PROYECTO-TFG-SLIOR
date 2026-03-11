package com.slior.dto.route;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StopRequest(

        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        @NotBlank(message = "El destinatario es obligatorio")
        String destinatario,

        @NotBlank(message = "El teléfono del destinatario es obligatorio")
        String telefonoDestinatario,

        @NotNull(message = "La latitud es obligatoria")
        Double latitud,

        @NotNull(message = "La longitud es obligatoria")
        Double longitud,

        String notas
) {}