package com.slior.exception;

/**
 * Excepción lanzada cuando se intenta registrar
 * un email que ya existe en el sistema.
 * Mapeada a HTTP 400 en GlobalExceptionHandler.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("El email ya está registrado: " + email);
    }
}