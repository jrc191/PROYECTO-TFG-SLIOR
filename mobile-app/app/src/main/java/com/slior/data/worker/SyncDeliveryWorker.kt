package com.slior.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slior.data.local.dao.RouteDao
import com.slior.data.remote.ApiService
import com.slior.data.local.entity.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

@HiltWorker
class SyncDeliveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val routeDao: RouteDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val stopId = inputData.getString("stopId") ?: return Result.failure()
        
        return try {
            // Intentar actualizar en el servidor
            apiService.updateStopStatus(stopId, "ENTREGADO")
            
            // Si tiene éxito, marcar como sincronizado
            routeDao.updateStopSyncStatus(stopId, SyncStatus.SYNCED.name)
            Result.success()
        } catch (e: IOException) {
            // Error de red: reintentar más tarde (WorkManager se encarga)
            Result.retry()
        } catch (e: HttpException) {
            // Error del servidor (4xx, 5xx)
            if (e.code() in 400..499) {
                // Error de cliente/negocio: No reintentar, marcar como fallido
                routeDao.updateStopSyncStatus(stopId, SyncStatus.FAILED.name)
                Result.failure()
            } else {
                // 5xx: Reintentar
                Result.retry()
            }
        } catch (e: Exception) {
            // Error inesperado: marcar como fallido para que deje de girar
            routeDao.updateStopSyncStatus(stopId, SyncStatus.FAILED.name)
            Result.failure()
        }
    }
}
