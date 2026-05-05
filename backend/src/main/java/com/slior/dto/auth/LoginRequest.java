package com.slior.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el endpoint POST /auth/login.
 * Usa Java Record (inmutable, sin boilerplate).
 */
public record LoginRequest(

        @Email(message = "El email no tiene un formato válido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password
) {}
