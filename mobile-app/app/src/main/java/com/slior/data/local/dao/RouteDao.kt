package com.slior.data.local.dao

import androidx.room.*
import com.slior.data.local.entity.RouteEntity
import com.slior.data.local.entity.StopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Query("SELECT * FROM routes WHERE repartidorId = :repartidorId ORDER BY fechaPlanificada ASC")
    fun getRoutesByRepartidor(repartidorId: String): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: String): RouteEntity?

    @Query("SELECT * FROM stops WHERE routeId = :routeId ORDER BY ordenVisita ASC")
    fun getStopsByRoute(routeId: String): Flow<List<StopEntity>>

    @Query("SELECT * FROM stops WHERE routeId = :routeId")
    suspend fun getStopsByRouteSync(routeId: String): List<StopEntity>

    @Query("SELECT * FROM stops WHERE id = :id")
    suspend fun getStopById(id: String): StopEntity?

    @Query("SELECT * FROM stops WHERE id = :id")
    fun observeStopById(id: String): Flow<StopEntity?>

    @Query("UPDATE stops SET status = :status, entregadoEn = :timestamp, syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateStopStatus(id: String, status: String, timestamp: String, syncStatus: String)

    @Query("UPDATE stops SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateStopSyncStatus(id: String, syncStatus: String)

    @Query("SELECT * FROM stops WHERE syncStatus IN ('PENDING', 'FAILED')")
    suspend fun getPendingStops(): List<StopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Query("DELETE FROM routes WHERE repartidorId = :repartidorId")
    suspend fun deleteRoutesForRepartidor(repartidorId: String)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRouteById(id: String)

    @Query("DELETE FROM stops WHERE routeId = :routeId")
    suspend fun deleteStopsByRouteId(routeId: String)
}