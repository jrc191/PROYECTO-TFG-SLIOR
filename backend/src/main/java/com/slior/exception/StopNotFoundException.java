package com.slior.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StopNotFoundException extends RuntimeException {
    public StopNotFoundException(String id) {
        super("Parada no encontrada con ID: " + id);
    }
}
