package com.slior.data.repository

import com.slior.data.remote.dto.AddressSuggestion
import com.slior.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.slior.util.RealAddresses
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodeService @Inject constructor() {
    private val cache = mutableMapOf<String, List<AddressSuggestion>>()

    suspend fun searchAddresses(query: String): Result<List<AddressSuggestion>> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return Result.Success(emptyList())
        if (normalizedQuery.length < 3) return Result.Success(emptyList())

        cache[normalizedQuery]?.let { return Result.Success(it) }

        return withContext(Dispatchers.IO) {
            try {
                val matches = RealAddresses.spanishAddresses
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

                cache[normalizedQuery] = matches
                Result.Success(matches)
            } catch (e: Exception) {
                Result.Error(Exception("No se pudieron cargar direcciones", e))
            }
        }
    }

    fun clearCache() {
        cache.clear()
    }
}
