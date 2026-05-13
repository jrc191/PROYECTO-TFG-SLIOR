package com.slior.exception;

/**
 * Excepción lanzada cuando las credenciales no coinciden.
 * Mapeada a HTTP 401 en GlobalExceptionHandler.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email o contraseña incorrectos");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
