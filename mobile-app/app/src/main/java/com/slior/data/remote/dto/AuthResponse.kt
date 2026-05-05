package com.slior.data.remote.dto

/** Respuesta del servidor al hacer login o register. Contiene el JWT. */
data class AuthResponse(
    val token: String,
    val type: String,
    val userId: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val vehicleType: String = "CAR"
)