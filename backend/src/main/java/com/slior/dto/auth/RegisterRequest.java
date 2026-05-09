package com.slior.dto.auth;

import com.slior.model.enums.UserRole;
import com.slior.model.enums.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el endpoint POST /auth/register.
 */
public record RegisterRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Email(message = "El email no tiene un formato válido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        UserRole rol,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        VehicleType vehicleType
) {}
