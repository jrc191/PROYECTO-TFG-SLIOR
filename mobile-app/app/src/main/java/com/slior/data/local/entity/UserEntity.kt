package com.slior.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un usuario en la base de datos local (SQLite).
 * El ID es un UUID almacenado como String (Room no tiene tipo UUID nativo).
 * syncStatus indica si el registro está sincronizado con el servidor.
 */
@Entity(tableName = "users")
data class UserEntity(

    @PrimaryKey
    val id: String,                    // UUID como String

    val nombre: String,

    val email: String,

    val rol: String,                   // "REPARTIDOR" o "ADMINISTRADOR"

    val vehicleType: String = VehicleType.VAN.name,  // Tipo de vehículo (CAR, VAN, TRUCK) - VAN por defecto

    val timestamp: Long = System.currentTimeMillis(),

    val syncStatus: String = SyncStatus.SYNCED.name
)