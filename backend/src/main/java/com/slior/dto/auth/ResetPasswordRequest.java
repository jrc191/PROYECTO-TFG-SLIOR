package com.slior.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para resetear la contraseña con el código recibido.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "El código es obligatorio")
        String token,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String newPassword
) {}
