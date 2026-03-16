package com.slior.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slior.data.repository.AuthRepository
import com.slior.ui.auth.LoginState
import com.slior.ui.auth.ServerStatus
import com.slior.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Checking)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus

    init {
        checkServerConnectivity()
    }

    /** Comprueba la conectividad con el servidor (se puede rellamar al recargar pantalla). */
    fun checkServerConnectivity() {
        viewModelScope.launch {
            _serverStatus.value = ServerStatus.Checking
            _serverStatus.value = authRepository.checkServerStatus()
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Completa email y contraseña")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            _loginState.value = when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _serverStatus.value = ServerStatus.Online
                    LoginState.Success(result.data)
                }
                is Result.Error -> {
                    val msg = result.exception.toUserMessage(isLogin = true)
                    if (result.exception is IOException) _serverStatus.value = ServerStatus.Offline
                    LoginState.Error(msg)
                }
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
                is Result.Success -> {
                    _serverStatus.value = ServerStatus.Online
                    LoginState.Success(result.data)
                }
                is Result.Error -> {
                    val msg = result.exception.toUserMessage(isLogin = false)
                    if (result.exception is IOException) _serverStatus.value = ServerStatus.Offline
                    LoginState.Error(msg)
                }
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

//  Clasificación de errores en mensajes legibles 
private fun Exception.toUserMessage(isLogin: Boolean): String = when (this) {
    is UnknownHostException  -> "Sin conexión al servidor. Comprueba tu red"
    is SocketTimeoutException -> "Tiempo de espera agotado. Inténtalo de nuevo"
    is IOException           -> "Error de conexión. Inténtalo de nuevo"
    is HttpException         -> when (code()) {
        400  -> if (isLogin) "Email o contraseña inválidos" else "Datos incorrectos o incompletos"
        401  -> "Email o contraseña incorrectos"
        403  -> "Acceso denegado"
        409  -> "Este email ya está registrado"
        422  -> "Los datos no cumplen los requisitos"
        500, 502, 503 -> "Error interno del servidor. Inténtalo más tarde"
        else -> "Error del servidor (${code()})"
    }
    else -> "Error inesperado. Inténtalo de nuevo"
}