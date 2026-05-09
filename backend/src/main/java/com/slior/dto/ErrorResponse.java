package com.slior.dto;

import java.time.Instant;

/**
 * DTO estandarizado para respuestas de error.
 * Incluye el requestId para trazabilidad (Correlation ID).
 */
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String requestId
) {
    public static ErrorResponse of(int status, String error, String message, String path, String requestId) {
        return new ErrorResponse(
                Instant.now().toString(),
                status,
                error,
                message,
                path,
                requestId
        );
    }
}

