package com.slior.data.remote.dto

/** Cuerpo de la petición POST /auth/register */
data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String,           // "REPARTIDOR" o "ADMINISTRADOR"
    val vehicleType: String    // "CAR", "VAN", o "TRUCK"
)