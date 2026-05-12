package com.slior.data.repository

import android.content.Context
import androidx.work.*
import com.slior.data.local.dao.RouteDao
import com.slior.data.local.entity.RouteEntity
import com.slior.data.local.entity.StopEntity
import com.slior.data.local.entity.SyncStatus
import com.slior.data.remote.ApiService
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.remote.dto.RouteResponseDto
import com.slior.data.remote.dto.OptimizeRouteRequest
import com.slior.data.remote.dto.UpdateRouteRequest
import com.slior.data.worker.SyncDeliveryWorker
import com.slior.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    private val routeDao: RouteDao,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {

    fun getRoutesByRepartidor(repartidorId: String): Flow<List<RouteEntity>> =
        routeDao.getRoutesByRepartidor(repartidorId)

    fun getStopsByRoute(routeId: String): Flow<List<StopEntity>> =
        routeDao.getStopsByRoute(routeId)

    suspend fun confirmDeliveryOptimistic(stopId: String, timestamp: String): Result<Unit> {
        return try {
            // 1. Actualizar localmente con estado PENDING
            routeDao.updateStopStatus(stopId, "ENTREGADO", timestamp, SyncStatus.PENDING.name)
            
            // 2. Programar sincronización en background
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = OneTimeWorkRequestBuilder<SyncDeliveryWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("stopId" to stopId))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
                
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync_$stopId",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun syncRoutes(repartidorId: String): Result<Unit> {
        return try {
            val remoteRoutes = apiService.getRoutesByRepartidor(repartidorId)
            
            // 1. Obtener paradas pendientes de sincronizar antes de borrar
            val pendingStops = routeDao.getPendingStops().associateBy { it.id }

            routeDao.deleteRoutesForRepartidor(repartidorId)
            routeDao.insertRoutes(remoteRoutes.map { it.toRouteEntity() })
            
            // 2. Insertar paradas nuevas pero preservando el estado local de las pendientes
            val stopsToInsert = remoteRoutes.flatMap { route ->
                route.paradas.map { dto ->
                    val entity = dto.toStopEntity(route.id)
                    pendingStops[entity.id] ?: entity
                }
            }
            routeDao.insertStops(stopsToInsert)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun syncStopStatus(stopId: String, status: String): Result<Unit> {
        return try {
            // Update backend
            apiService.updateStopStatus(stopId, status)
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

    suspend fun updateRoute(routeId: String, request: UpdateRouteRequest): Result<Unit> {
        return try {
            val updated = apiService.updateRoute(routeId, request)
            // Actualizar localmente
            routeDao.insertRoutes(listOf(updated.toRouteEntity()))
            // Para las paradas, borramos las antiguas de esta ruta y ponemos las nuevas
            // (Simulando el comportamiento del backend para consistencia)
            routeDao.deleteStopsByRouteId(routeId)
            routeDao.insertStops(updated.paradas.map { it.toStopEntity(updated.id) })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteRoute(routeId: String): Result<Unit> {
        return try {
            apiService.deleteRoute(routeId)
            routeDao.deleteRouteById(routeId)
            routeDao.deleteStopsByRouteId(routeId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun optimizeRoute(routeId: String, lat: Double, lon: Double, vehicleType: String): Result<Unit> {
        return try {
            // 1. Obtener estados locales actuales para preservarlos
            val localStops = routeDao.getStopsByRouteSync(routeId)
            val deliveredIds = localStops.filter { it.status == "ENTREGADO" }.map { it.id }.toSet()

            val request = OptimizeRouteRequest(
                puntoInicioLat = lat,
                puntoInicioLon = lon,
                vehicleType = vehicleType
            )
            val optimized = apiService.optimizeRoute(routeId, request)
            
            // 2. Mapear DTOs a Entidades preservando el estado local
            val optimizedStops = optimized.paradas.map { dto ->
                val entity = dto.toStopEntity(optimized.id)
                if (deliveredIds.contains(entity.id)) {
                    entity.copy(status = "ENTREGADO")
                } else {
                    entity
                }
            }

            // 3. Guardar cambios
            routeDao.insertRoutes(listOf(optimized.toRouteEntity()))
            routeDao.insertStops(optimizedStops)
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
            entregadoEn = entregadoEn,
            codigoPaquete = codigoPaquete
        )
}