package com.slior.exception;

/**
 * Excepción lanzada cuando el email o la contraseña
 * no coinciden durante el login.
 * Mapeada a HTTP 401 en GlobalExceptionHandler.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email o contraseña incorrectos");
    }
}