package com.slior.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StopResponseDto(
    val id: String,
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

data class RouteResponseDto(
    val id: String,
    val nombre: String,
    val fechaPlanificada: String,
    val status: String,
    val repartidorId: String,
    val repartidorNombre: String,
    val paradas: List<StopResponseDto>,
    val distanciaTotal: Double?,
    val notas: String?,
    val createdAt: String,
    val tiempoEstimado: Int?
)