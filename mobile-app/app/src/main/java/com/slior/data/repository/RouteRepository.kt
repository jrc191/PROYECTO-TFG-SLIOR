package com.slior.data.repository

import com.slior.data.local.dao.RouteDao
import com.slior.data.local.entity.RouteEntity
import com.slior.data.local.entity.StopEntity
import com.slior.data.remote.ApiService
import com.slior.data.remote.dto.CreateRouteRequest
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

    fun getStopsByRoute(routeId: String): Flow<List<StopEntity>> =
        routeDao.getStopsByRoute(routeId)

    suspend fun syncRoutes(repartidorId: String): Result<Unit> {
        return try {
            val remoteRoutes = apiService.getRoutesByRepartidor(repartidorId)
            routeDao.deleteRoutesForRepartidor(repartidorId)
            routeDao.insertRoutes(remoteRoutes.map { it.toRouteEntity() })
            routeDao.insertStops(remoteRoutes.flatMap { route ->
                route.paradas.map { it.toStopEntity(route.id) }
            })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createRoute(request: CreateRouteRequest): Result<Unit> {
        return try {
            val created = apiService.createRoute(request)
            routeDao.insertRoutes(listOf(created.toRouteEntity()))
            routeDao.insertStops(created.paradas.map { it.toStopEntity(created.id) })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun optimizeRoute(routeId: String, lat: Double, lon: Double): Result<Unit> {
        return try {
            val optimized = apiService.optimizeRoute(
                routeId,
                mapOf("puntoInicioLat" to lat, "puntoInicioLon" to lon)
            )
            routeDao.insertRoutes(listOf(optimized.toRouteEntity()))
            routeDao.insertStops(optimized.paradas.map { it.toStopEntity(optimized.id) })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // --- Conversiones DTO → Entity ---

    private fun RouteResponseDto.toRouteEntity() = RouteEntity(
        id = id,
        nombre = nombre,
        fechaPlanificada = fechaPlanificada,
        status = status,
        repartidorId = repartidorId,
        repartidorNombre = repartidorNombre,
        distanciaTotal = distanciaTotal,
        tiempoEstimado = tiempoEstimado,
        notas = notas,
        createdAt = createdAt
    )

    private fun com.slior.data.remote.dto.StopResponseDto.toStopEntity(routeId: String) =
        StopEntity(
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