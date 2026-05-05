package com.slior.controller;

import com.slior.dto.auth.AuthResponse;
import com.slior.dto.auth.LoginRequest;
import com.slior.dto.auth.RegisterRequest;
import com.slior.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para autenticación.
 * Endpoints públicos (no requieren JWT).
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
}