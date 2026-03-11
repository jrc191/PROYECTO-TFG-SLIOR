package com.slior.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slior.data.repository.AuthRepository
import com.slior.ui.auth.LoginState
import com.slior.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado observable por la UI
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email y contraseña son obligatorios")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            _loginState.value = when (val result = authRepository.login(email, password)) {
                is Result.Success -> LoginState.Success(result.data)
                is Result.Error -> LoginState.Error(
                    result.exception.message ?: "Error de conexión"
                )
                is Result.Loading -> LoginState.Loading
            }
        }
    }

    fun register(nombre: String, email: String, password: String, rol: String) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            _loginState.value = when (val result = authRepository.register(nombre, email, password, rol)) {
                is Result.Success -> LoginState.Success(result.data)
                is Result.Error -> LoginState.Error(
                    result.exception.message ?: "Error de registro"
                )
                is Result.Loading -> LoginState.Loading
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    fun setError(message: String) {
        _loginState.value = LoginState.Error(message)
    }
}