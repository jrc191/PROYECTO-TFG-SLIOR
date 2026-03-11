package com.slior.data.repository

import com.slior.data.local.dao.RouteDao
import com.slior.data.local.entity.RouteEntity
import com.slior.data.local.entity.StopEntity
import com.slior.data.remote.ApiService
import com.slior.data.remote.dto.RouteResponseDto
import com.slior.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    private val routeDao: RouteDao,
    private val apiService: ApiService
) {

    fun getRoutesByRepartidor(repartidorId: String): Flow<List<RouteEntity>> =
        routeDao.getRoutesByRepartidor(repartidorId)

    suspend fun syncRoutes(repartidorId: String): Result<Unit> {
        return try {
            val remoteRoutes = apiService.getRoutesByRepartidor(repartidorId)
            routeDao.deleteRoutesForRepartidor(repartidorId)
            routeDao.insertRoutes(remoteRoutes.map { it.toEntity() })
            val stops = remoteRoutes.flatMap { route ->
                route.paradas.map { it.toEntity(route.id) }
            }
            routeDao.insertStops(stops)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun RouteResponseDto.toEntity() = RouteEntity(
        id = id,
        nombre = nombre,
        fechaPlanificada = fechaPlanificada,
        status = status,
        repartidorId = repartidorId,
        repartidorNombre = repartidorNombre,
        distanciaTotal = distanciaTotal,
        notas = notas,
        createdAt = createdAt
    )

    private fun com.slior.data.remote.dto.StopResponseDto.toEntity(routeId: String) = StopEntity(
        id = id,
        routeId = routeId,
        direccion = direccion,
        destinatario = destinatario,
        telefonoDestinatario = telefonoDestinatario,
        latitud = latitud,
        longitud = longitud,
        ordenVisita = ordenVisita,
        status = status,
        notas = notas,
        entregadoEn = entregadoEn
    )
}