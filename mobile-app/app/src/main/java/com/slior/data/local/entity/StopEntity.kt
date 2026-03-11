package com.slior.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey
    val id: String,
    val routeId: String,
    val direccion: String,
    val destinatario: String,
    val telefonoDestinatario: String,
    val latitud: Double,
    val longitud: Double,
    val ordenVisita: Int,
    val status: String,
    val notas: String?,
    val entregadoEn: String?
)
