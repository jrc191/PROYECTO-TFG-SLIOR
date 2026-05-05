package com.slior.data.remote.dto

/**
 * DTO para la solicitud de optimización de ruta.
 * Incluye la ubicación del inicio y el tipo de vehículo para optimizar según velocidad.
 */
data class OptimizeRouteRequest(
    val puntoInicioLat: Double,
    val puntoInicioLon: Double,
    val vehicleType: String
)
