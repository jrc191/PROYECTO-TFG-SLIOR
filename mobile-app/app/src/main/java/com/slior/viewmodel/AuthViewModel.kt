package com.slior.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.slior.data.remote.AuthInterceptor.Companion.TOKEN_KEY
import com.slior.data.remote.dataStore
import com.slior.data.repository.AuthRepository
import com.slior.ui.auth.LoginState
import com.slior.ui.auth.ServerStatus
import com.slior.util.GlobalEventBus
import com.slior.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
    private val globalEventBus: GlobalEventBus
) : ViewModel() {

    private val _unauthorizedEvent = MutableStateFlow(false)
    val unauthorizedEvent: StateFlow<Boolean> = _unauthorizedEvent

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Checking)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus

    private val _sessionUserId = MutableStateFlow<String?>(null)
    val sessionUserId: StateFlow<String?> = _sessionUserId

    init {
        checkExistingSession()
        checkServerConnectivity()
        observeUnauthorizedEvents()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            globalEventBus.connectivityEvent.collect { isConnected ->
                if (isConnected) {
                    checkServerConnectivity()
                } else {
                    _serverStatus.value = ServerStatus.Offline
                }
            }
        }
    }

    private fun observeUnauthorizedEvents() {
        viewModelScope.launch {
            globalEventBus.unauthorizedEvent.collect {
                _unauthorizedEvent.value = true
            }
        }
    }

    fun consumeUnauthorizedEvent() {
        _unauthorizedEvent.value = false
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                val prefs = context.dataStore.data.first()
                val token = prefs[TOKEN_KEY]

                if (!token.isNullOrBlank()) {
                    val userId = authRepository.getSavedUserId()
                    if (!userId.isNullOrBlank()) {
                        _sessionUserId.value = userId
                        return@launch
                    }
                }
                _sessionUserId.value = ""
            } catch (e: Exception) {
                _sessionUserId.value = ""
            }
        }
    }

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
                    _sessionUserId.value = result.data
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

    fun register(nombre: String, email: String, password: String, rol: String, vehicleType: String) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Todos los campos son obligatorios")
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            _loginState.value = when (val result = authRepository.register(nombre, email, password, rol, vehicleType)) {
                is Result.Success -> {
                    _serverStatus.value = ServerStatus.Online
                    _sessionUserId.value = result.data
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

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _sessionUserId.value = ""
            _loginState.value = LoginState.Idle
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    fun setError(message: String) {
        _loginState.value = LoginState.Error(message)
    }

    private fun Exception.toUserMessage(isLogin: Boolean): String = when (this) {
        is UnknownHostException   -> "Sin conexión al servidor"
        is SocketTimeoutException -> "Tiempo de espera agotado"
        is IOException            -> "Error de conexión"
        is HttpException          -> {
            val errorBody = response()?.errorBody()?.string()
            val backendMessage = try {
                val map = Gson().fromJson(errorBody, Map::class.java)
                map["message"] as? String
            } catch (e: Exception) {
                null
            }

            backendMessage ?: when (code()) {
                401  -> if (isLogin) "Email o contraseña incorrectos" else "Sesión no válida o expirada"
                409  -> "Este email ya está registrado"
                422  -> "Los datos no cumplen los requisitos"
                in 500..599 -> "Error del servidor. Inténtalo más tarde"
                else -> "Error del servidor (${code()})"
            }
        }
        else -> "Error inesperado"
    }
}
