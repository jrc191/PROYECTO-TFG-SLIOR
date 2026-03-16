package com.slior.data.repository

import android.util.Log
import com.slior.data.remote.ApiService
import com.slior.data.remote.PhotonApiService
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodeService @Inject constructor(
    private val apiService: ApiService,
    private val photonApiService: PhotonApiService
) {
    private val tag = "GeocodeService"
    private val cache = mutableMapOf<String, List<AddressSuggestion>>()
    private var lastRequestTime = 0L

    suspend fun searchAddresses(query: String): Result<List<AddressSuggestion>> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return Result.Success(emptyList())
        if (normalizedQuery.length < 3) return Result.Success(emptyList())

        cache[normalizedQuery]?.let { return Result.Success(it) }

        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val timeSinceLastRequest = now - lastRequestTime
                if (timeSinceLastRequest < 1000) {
                    delay(1000 - timeSinceLastRequest)
                }

                lastRequestTime = System.currentTimeMillis()
                val backendResults = withTimeout(10000L) {
                    apiService.searchAddresses(normalizedQuery)
                }

                var finalResults = if (backendResults.isNotEmpty()) {
                    Log.d(tag, "Backend geocode OK for '$normalizedQuery': ${backendResults.size} results")
                    backendResults
                } else {
                    Log.w(tag, "Backend geocode empty for '$normalizedQuery', probando Photon local")
                    val photonResults = try {
                        withTimeout(7000L) {
                            photonApiService.searchAddresses(normalizedQuery)
                                .features
                                ?.mapNotNull { it.toAddressSuggestionOrNull() }
                                ?: emptyList()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(tag, "Photon fallback fallo para '$normalizedQuery'", e)
                        emptyList()
                    }
                    if (photonResults.isNotEmpty()) {
                        Log.d(tag, "Photon devolvió ${photonResults.size} resultados para '$normalizedQuery'")
                    }
                    photonResults
                }

                if (finalResults.isEmpty()) {
                    Log.w(tag, "Sin resultados para '$normalizedQuery' tras backend y Photon")
                    finalResults = emptyList()
                }

                cache[normalizedQuery] = finalResults
                Result.Success(finalResults)
            } catch (e: TimeoutCancellationException) {
                Log.w(tag, "Backend geocode TIMEOUT for '$normalizedQuery'", e)
                Result.Error(Exception("Tiempo de espera agotado buscando direcciones", e))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Backend geocode ERROR for '$normalizedQuery'", e)
                Result.Error(Exception("No se pudieron cargar direcciones", e))
            }
        }
    }

    fun clearCache() {
        cache.clear()
    }
}
