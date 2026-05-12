package com.slior.dto.user;

import com.slior.model.Route;
import com.slior.model.Stop;
import com.slior.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO para el Derecho de Acceso/Portabilidad (RGPD).
 * Contiene toda la información que el sistema tiene sobre un usuario.
 */
@Data
@Builder
public class UserDataExportDto {
    private UserProfile profile;
    private List<RouteData> routes;

    @Data
    @Builder
    public static class UserProfile {
        private String nombre;
        private String email;
        private String rol;
        private String vehicleType;
        private String createdAt;
    }

    @Data
    @Builder
    public static class RouteData {
        private String nombre;
        private String fecha;
        private String status;
        private List<StopData> stops;
    }

    @Data
    @Builder
    public static class StopData {
        private String direccion;
        private String destinatario;
        private String status;
        private String entregadoEn;
    }
}
