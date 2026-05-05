package com.slior.ui.auth

// Estados posibles de la pantalla de Login.
// La UI observa este sealed interface y renderiza según el estado actual.
sealed interface LoginState {

    // Estado inicial: formulario vacío, sin actividad
    object Idle : LoginState

    // Petición en curso: deshabilitar botón, mostrar ProgressBar
    object Loading : LoginState

    // Login correcto: navegar a la pantalla principal
    data class Success(val repartidorId: String) : LoginState

    // Error: mostrar mensaje en Snackbar
    data class Error(val message: String) : LoginState
}