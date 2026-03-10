package com.slior.exception;

public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(String id) {
        super("Ruta no encontrada con ID: " + id);
    }
}