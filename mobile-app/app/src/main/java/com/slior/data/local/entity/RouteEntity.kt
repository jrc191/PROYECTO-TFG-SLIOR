package com.slior.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val fechaPlanificada: String,
    val status: String,
    val repartidorId: String,
    val repartidorNombre: String,
    val distanciaTotal: Double?,
    val notas: String?,
    val createdAt: String,
    val syncStatus: String = "SYNCED"
)
