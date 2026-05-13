package com.slior.data.remote.dto

data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val newPassword: String
)

data class UpdatePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
