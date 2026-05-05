package com.slior.util

// Sealed class para representar el resultado de cualquier operación asíncrona.
// La UI observa este tipo y reacciona según el estado: cargando, éxito o error.
sealed class Result<out T> {

    // Operación completada con éxito. Contiene el dato resultante.
    data class Success<T>(val data: T) : Result<T>()

    // Operación fallida. Contiene la excepción con el mensaje de error.
    data class Error(val exception: Exception) : Result<Nothing>()

    // Operación en curso. La UI muestra un indicador de carga.
    object Loading : Result<Nothing>()
}