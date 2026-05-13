package com.slior.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para cambiar la contraseña de un usuario ya logueado.
 */
public record UpdatePasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String oldPassword,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
        String newPassword
) {}
