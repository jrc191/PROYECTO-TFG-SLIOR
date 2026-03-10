package com.slior.data.remote.dto

data class StopRequestDto(
    val direccion: String,
    val destinatario: String,
    val telefonoDestinatario: String,
    val latitud: Double,
    val longitud: Double,
    val notas: String? = null
)

data class CreateRouteRequest(
    val nombre: String,
    val fechaPlanificada: String,
    val repartidorId: String,
    val paradas: List<StopRequestDto>,
    val notas: String? = null
)