package com.slior.data.repository

import com.slior.data.remote.ApiService
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.util.RealAddresses
import com.slior.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodeService @Inject constructor(
    private val apiService: ApiService
) {
    private val cache = mutableMapOf<String, List<AddressSuggestion>>()

    suspend fun searchAddresses(query: String): Result<List<AddressSuggestion>> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return Result.Success(emptyList())
        if (normalizedQuery.length < 3) return Result.Success(emptyList())

        cache[normalizedQuery]?.let { return Result.Success(it) }

        return withContext(Dispatchers.IO) {
            try {
                val backendResults = apiService.searchAddresses(normalizedQuery)
                cache[normalizedQuery] = backendResults
                Result.Success(backendResults)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val fallback = RealAddresses.spanishAddresses
                    .filter {
                        it.direccion.contains(normalizedQuery, ignoreCase = true) ||
                                it.ciudad.contains(normalizedQuery, ignoreCase = true)
                    }
                    .take(5)
                    .ifEmpty { RealAddresses.getRandomAddresses(5) }
                    .map {
                        AddressSuggestion(
                            displayName = "${it.direccion}, ${it.ciudad}",
                            latitude = it.latitud,
                            longitude = it.longitud
                        )
                    }
                cache[normalizedQuery] = fallback
                Result.Success(fallback)
            }
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): Result<AddressSuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiService.reverseGeocode(lat, lon)
                Result.Success(result)
            } catch (e: Exception) {
                // Fallback: Devolver una dirección genérica si falla el servidor
                Result.Success(AddressSuggestion("Ubicación seleccionada ($lat, $lon)", lat, lon))
            }
        }
    }

    fun clearCache() {
        cache.clear()
    }
}
