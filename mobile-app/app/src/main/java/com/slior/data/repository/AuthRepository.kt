package com.slior.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.slior.data.local.dao.UserDao
import com.slior.data.local.entity.UserEntity
import com.slior.data.remote.ApiService
import com.slior.data.remote.AuthInterceptor.Companion.TOKEN_KEY
import com.slior.data.remote.dto.*
import com.slior.data.remote.dataStore
import com.slior.util.Result
import com.slior.ui.auth.ServerStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

import androidx.datastore.preferences.core.stringPreferencesKey
import com.slior.data.remote.dto.*

// ... rest of imports

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
    }

    /**
     * Guarda la preferencia de tema (light, dark, system).
     */
    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    /**
     * Observa la preferencia de tema actual.
     */
    fun getTheme(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[THEME_KEY]
        }
    }

    // Hace login en el backend, guarda el token y el usuario en local
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            // Guardar JWT en DataStore
            context.dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = response.token
            }

            // Guardar usuario en Room (offline-first)
            // Si vehicleType es null, usar VAN como valor por defecto
            val vehicleType = response.vehicleType ?: "VAN"
            userDao.insert(
                UserEntity(
                    id = response.userId,
                    nombre = response.nombre,
                    email = response.email,
                    rol = response.rol,
                    vehicleType = vehicleType,
                    consentimientoNotificaciones = response.consentimientoNotificaciones
                )
            )

            Result.Success(response.userId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Registra un nuevo usuario en el backend
    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        rol: String,
        vehicleType: String
    ): Result<String> {
        return try {
            val response = apiService.register(
                RegisterRequest(nombre, email, password, rol, vehicleType)
            )

            context.dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = response.token
            }

            // Si vehicleType es null, usar VAN como valor por defecto
            val finalVehicleType = response.vehicleType ?: "VAN"
            userDao.insert(
                UserEntity(
                    id = response.userId,
                    nombre = response.nombre,
                    email = response.email,
                    rol = response.rol,
                    vehicleType = finalVehicleType,
                    consentimientoNotificaciones = response.consentimientoNotificaciones
                )
            )

            Result.Success(response.userId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Actualiza el consentimiento de notificaciones en el servidor y localmente.
     */
    suspend fun updateNotifications(userId: String, enabled: Boolean): Result<Unit> {
        return try {
            // 1. Actualizar en el servidor
            apiService.updateNotifications(enabled)
            
            // 2. Actualizar localmente
            userDao.updateNotificationConsent(userId, enabled)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Solicita código de restablecimiento.
     */
    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            apiService.forgotPassword(ForgotPasswordRequest(email))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Resetea la contraseña usando el código.
     */
    suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit> {
        return try {
            apiService.resetPassword(request)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Cambia la contraseña del usuario logueado.
     */
    suspend fun updatePassword(oldPass: String, newPass: String): Result<Unit> {
        return try {
            apiService.updatePassword(UpdatePasswordRequest(oldPass, newPass))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Devuelve el userId del usuario guardado en Room, o null si no hay sesión.
     */
    suspend fun getSavedUserId(): String? {
        return userDao.getFirstUserId()
    }

    // Cierra sesión: limpia token y datos locales
    suspend fun logout() {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
        userDao.deleteAll()
    }

    /** Intenta alcanzar el servidor. Cualquier respuesta HTTP = Online.
     *  Solo una IOException (sin red / host inaccesible) = Offline. */
    suspend fun checkServerStatus(): ServerStatus {
        return try {
            apiService.healthCheck()
            ServerStatus.Online
        } catch (e: HttpException) {
            ServerStatus.Online   // Hubo respuesta HTTP, el servidor está activo
        } catch (e: IOException) {
            ServerStatus.Offline
        } catch (e: Exception) {
            ServerStatus.Offline
        }
    }

    // Observa el usuario actual desde Room (Flow se actualiza automáticamente)
    fun getCurrentUser(userId: String): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }
}