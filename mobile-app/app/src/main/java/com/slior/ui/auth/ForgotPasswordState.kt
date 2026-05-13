package com.slior.ui.auth

/**
 * Estados para el flujo de restablecimiento de contraseña.
 */
sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object CodeSent : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}
