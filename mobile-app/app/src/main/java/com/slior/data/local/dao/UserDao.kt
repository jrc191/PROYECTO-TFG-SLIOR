package com.slior.data.local.dao

import androidx.room.*
import com.slior.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la entidad UserEntity.
 * Room genera automáticamente la implementación de estos métodos.
 * Las funciones suspend se ejecutan en background (no bloquean el hilo principal).
 * Flow emite nuevos valores automáticamente cuando cambian los datos.
 */
@Dao
interface UserDao {

    /** Inserta o reemplaza un usuario (útil al hacer login). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    /** Busca un usuario por email. Retorna null si no existe. */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    /** Observa un usuario por ID. Emite cada vez que cambie en la BD. */
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    /** Obtiene un usuario por ID de forma síncrona (suspend). */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: String): UserEntity?

    /** Actualiza el estado de las notificaciones para un usuario. */
    @Query("UPDATE users SET consentimientoNotificaciones = :enabled WHERE id = :userId")
    suspend fun updateNotificationConsent(userId: String, enabled: Boolean)

    /** Elimina todos los usuarios (usado al cerrar sesión). */
    @Query("DELETE FROM users")
    suspend fun deleteAll()

    /** Devuelve el ID del primer usuario guardado (el usuario logueado). */
    @Query("SELECT id FROM users LIMIT 1")
    suspend fun getFirstUserId(): String?
}