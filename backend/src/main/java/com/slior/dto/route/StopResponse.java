package com.slior.dto.route;

import com.slior.model.Stop;
import com.slior.model.enums.StopStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StopResponse(
        UUID id,
        String direccion,
        String destinatario,
        String telefonoDestinatario,
        Double latitud,
        Double longitud,
        Integer ordenVisita,
        StopStatus status,
        String notas,
        LocalDateTime entregadoEn
) {
    public static StopResponse from(Stop stop) {
        return new StopResponse(
                stop.getId(),
                stop.getDireccion(),
                stop.getDestinatario(),
                stop.getTelefonoDestinatario(),
                stop.getLatitud(),
                stop.getLongitud(),
                stop.getOrdenVisita(),
                stop.getStatus(),
                stop.getNotas(),
                stop.getEntregadoEn()
        );
    }
}