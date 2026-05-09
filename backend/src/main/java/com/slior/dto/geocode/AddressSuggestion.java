package com.slior.dto.geocode;

/**
 * DTO para representar una sugerencia de dirección.
 */
public record AddressSuggestion(
    Long id,
    String nombre,
    String numero,
    String municipio,
    String provincia,
    String codigoPostal,
    Double latitud,
    Double longitud
) {}

