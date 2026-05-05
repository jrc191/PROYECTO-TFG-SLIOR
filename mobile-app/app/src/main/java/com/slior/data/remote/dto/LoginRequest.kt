package com.slior.data.remote.dto

/** Cuerpo de la petición POST /auth/login */
data class LoginRequest(
    val email: String,
    val password: String
)