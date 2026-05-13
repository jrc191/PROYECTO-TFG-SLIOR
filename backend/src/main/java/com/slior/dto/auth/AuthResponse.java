package com.slior.dto.auth;

import com.slior.model.enums.UserRole;
import com.slior.model.enums.VehicleType;

import java.util.UUID;

/**
 * DTO de salida para los endpoints de autenticación.
 * Contiene el JWT, datos básicos del usuario, y su tipo de vehículo.
 * NUNCA incluye el password.
 */
public record AuthResponse(

        String token,

        String type,       // Siempre "Bearer"

        UUID userId,

        String nombre,

        String email,

        UserRole rol,

        VehicleType vehicleType,

        Boolean consentimientoNotificaciones
) {}
