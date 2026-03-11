package com.slior.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.slior.data.local.dao.UserDao
import com.slior.data.local.entity.UserEntity
import com.slior.data.remote.ApiService
import com.slior.data.remote.AuthInterceptor.Companion.TOKEN_KEY
import com.slior.data.remote.dto.LoginRequest
import com.slior.data.remote.dto.RegisterRequest
import com.slior.data.remote.dataStore
import com.slior.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) {

    // Hace login en el backend, guarda el token y el usuario en local
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            // Guardar JWT en DataStore
            context.dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = response.token
            }

            // Guardar usuario en Room (offline-first)
            userDao.insert(
                UserEntity(
                    id = response.userId,
                    nombre = response.nombre,
                    email = response.email,
                    rol = response.rol
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
        rol: String
    ): Result<String> {
        return try {
            val response = apiService.register(
                RegisterRequest(nombre, email, password, rol)
            )

            context.dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = response.token
            }

            userDao.insert(
                UserEntity(
                    id = response.userId,
                    nombre = response.nombre,
                    email = response.email,
                    rol = response.rol
                )
            )

            Result.Success(response.userId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Cierra sesión: limpia token y datos locales
    suspend fun logout() {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
        userDao.deleteAll()
    }

    // Observa el usuario actual desde Room (Flow se actualiza automáticamente)
    fun getCurrentUser(userId: String): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }
}