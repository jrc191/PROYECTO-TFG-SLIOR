package com.slior.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.slior.data.remote.AuthInterceptor.Companion.TOKEN_KEY
import com.slior.data.remote.dto.*
import com.slior.data.remote.dataStore
import com.slior.data.repository.AuthRepository
import com.slior.ui.auth.ForgotPasswordState
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

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
    private val globalEventBus: GlobalEventBus
) : ViewModel() {

    // Observa el tema desde DataStore y lo convierte en un StateFlow
    val appTheme: StateFlow<String> = authRepository.getTheme()
        .map { it ?: "system" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    /**
     * Guarda la preferencia del tema.
     */
    fun saveTheme(theme: String) {
        viewModelScope.launch {
            authRepository.saveTheme(theme)
        }
    }

    private val _unauthorizedEvent = MutableStateFlow(false)
    val unauthorizedEvent: StateFlow<Boolean> = _unauthorizedEvent

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState

    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Checking)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus

    private val _sessionUserId = MutableStateFlow<String?>(null)
    val sessionUserId: StateFlow<String?> = _sessionUserId

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<com.slior.data.local.entity.UserEntity?> = _sessionUserId
        .flatMapLatest { id: String? ->
            if (id.isNullOrBlank()) kotlinx.coroutines.flow.flowOf(null)
            else authRepository.getCurrentUser(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var connectivityJob: kotlinx.coroutines.Job? = null

    init {
        android.util.Log.d("AuthViewModel", "Initializing AuthViewModel: $this")
        checkExistingSession()
        // No llamamos a checkServerConnectivity() aquí, porque observeConnectivity() 
        // lo hará automáticamente al suscribirse (debido al replay=1 de connectivityEvent)
        observeUnauthorizedEvents()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            globalEventBus.connectivityEvent.collect { isConnected ->
                android.util.Log.d("AuthViewModel", "Connectivity changed: $isConnected")
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
                logout()
            }
        }
    }

    fun consumeUnauthorizedEvent() {
        _unauthorizedEvent.value = false
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Checking existing session...")
                val prefs = context.dataStore.data.first()
                val token = prefs[TOKEN_KEY]

                if (!token.isNullOrBlank()) {
                    android.util.Log.d("AuthViewModel", "Token found, checking userId...")
                    val userId = authRepository.getSavedUserId()
                    if (!userId.isNullOrBlank()) {
                        android.util.Log.d("AuthViewModel", "Session restored for user: $userId")
                        _sessionUserId.value = userId
                        _authState.value = AuthState.Authenticated(userId)
                        return@launch
                    } else {
                        android.util.Log.w("AuthViewModel", "Token present but no userId found in local DB")
                    }
                } else {
                    android.util.Log.d("AuthViewModel", "No token found in DataStore")
                }
                _sessionUserId.value = ""
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error checking session: ${e.message}", e)
                _sessionUserId.value = ""
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun checkServerConnectivity() {
        // Cancelar cualquier comprobación anterior para evitar concurrencia/spam
        connectivityJob?.cancel()
        connectivityJob = viewModelScope.launch {
            _serverStatus.value = ServerStatus.Checking
            val status = authRepository.checkServerStatus()
            android.util.Log.d("AuthViewModel", "Server status result: $status")
            _serverStatus.value = status
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
                    _authState.value = AuthState.Authenticated(result.data)
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
                    _authState.value = AuthState.Authenticated(result.data)
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

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordState.value = ForgotPasswordState.Error("Ingresa tu email")
            return
        }
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            _forgotPasswordState.value = when (val result = authRepository.forgotPassword(email)) {
                is Result.Success -> ForgotPasswordState.CodeSent
                is Result.Error -> {
                    val msg = result.exception.toUserMessage(isLogin = false)
                    ForgotPasswordState.Error(msg)
                }
                else -> ForgotPasswordState.Idle
            }
        }
    }

    fun resetPassword(email: String, code: String, newPass: String) {
        if (email.isBlank() || code.isBlank() || newPass.isBlank()) {
            _forgotPasswordState.value = ForgotPasswordState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            val request = ResetPasswordRequest(email, code, newPass)
            _forgotPasswordState.value = when (val result = authRepository.resetPassword(request)) {
                is Result.Success -> ForgotPasswordState.Success
                is Result.Error -> {
                    val msg = result.exception.toUserMessage(isLogin = false)
                    ForgotPasswordState.Error(msg)
                }
                else -> ForgotPasswordState.Idle
            }
        }
    }

    fun updatePassword(oldPass: String, newPass: String) {
        if (oldPass.isBlank() || newPass.isBlank()) {
            _forgotPasswordState.value = ForgotPasswordState.Error("Completa ambos campos")
            return
        }
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            _forgotPasswordState.value = when (val result = authRepository.updatePassword(oldPass, newPass)) {
                is Result.Success -> ForgotPasswordState.Success
                is Result.Error -> {
                    val msg = result.exception.toUserMessage(isLogin = false)
                    ForgotPasswordState.Error(msg)
                }
                else -> ForgotPasswordState.Idle
            }
        }
    }

    /**
     * Alterna el estado de las notificaciones.
     */
    fun updateNotifications(userId: String, enabled: Boolean) {
        viewModelScope.launch {
            authRepository.updateNotifications(userId, enabled)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _sessionUserId.value = ""
            _authState.value = AuthState.Unauthenticated
            _loginState.value = LoginState.Idle
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
        _forgotPasswordState.value = ForgotPasswordState.Idle
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

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
}
