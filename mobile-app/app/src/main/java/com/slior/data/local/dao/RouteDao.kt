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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Query("DELETE FROM routes WHERE repartidorId = :repartidorId")
    suspend fun deleteRoutesForRepartidor(repartidorId: String)
}