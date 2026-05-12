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
                val simplifiedResults = backendResults.map { it.copy(displayName = simplifyAddress(it.displayName)) }
                cache[normalizedQuery] = simplifiedResults
                Result.Success(simplifiedResults)
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
                Result.Success(result.copy(displayName = simplifyAddress(result.displayName)))
            } catch (e: Exception) {
                // Fallback: Devolver una dirección genérica si falla el servidor
                Result.Success(AddressSuggestion("Ubicación seleccionada ($lat, $lon)", lat, lon))
            }
        }
    }

    /**
     * Simplifica una dirección de OSM (que suele ser muy larga) a algo más legible.
     * Ejemplo: "Calle Caganche, Zalamea la Real, Cuenca Minera, Huelva, Andalucía, 21640, España"
     * Resultado: "Calle Caganche, Zalamea la Real"
     */
    private fun simplifyAddress(fullAddress: String): String {
        val parts = fullAddress.split(",")
        if (parts.size <= 2) return fullAddress
        
        // Intentamos quedarnos solo con los dos primeros componentes (Calle y Ciudad)
        // y limpiar espacios extra
        return "${parts[0].trim()}, ${parts[1].trim()}"
    }

    fun clearCache() {
        cache.clear()
    }
}
