package com.slior.data.remote.dto

import com.slior.data.remote.dto.StopRequestDto

/**
 * DTO para actualizar una ruta existente.
 */
data class UpdateRouteRequest(
    val nombre: String,
    val fechaPlanificada: String,
    val notas: String? = null,
    val paradas: List<StopRequestDto>
)
