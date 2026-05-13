package com.slior.controller;

import com.slior.dto.auth.*;
import com.slior.model.User;
import com.slior.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 * Gestiona login, registro y cambios de contraseña.
 */
@RestController
@RequestMapping("/auth/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/v1/register
     * Registra un nuevo usuario y retorna JWT.
     * Responde 201 Created.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    /**
     * POST /auth/v1/login
     * Autentica usuario y retorna JWT.
     * Responde 200 OK.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /auth/v1/forgot-password
     * Solicita restablecimiento de contraseña.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /auth/v1/reset-password
     * Procesa el cambio de contraseña con el código recibido por email.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH /auth/v1/update-password
     * Cambia la contraseña del usuario autenticado (requiere JWT).
     */
    @PatchMapping("/update-password")
    public ResponseEntity<Void> updatePassword(
            @RequestBody @Valid UpdatePasswordRequest request,
            Authentication authentication) {
        
        // El principal contiene el email del usuario autenticado
        String email = authentication.getName();
        User user = authService.getUserByEmail(email);
        
        authService.updatePassword(user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
