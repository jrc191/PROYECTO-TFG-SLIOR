package com.slior.data.repository

import com.slior.data.remote.NominatimApiService
import com.slior.data.remote.dto.AddressSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodeService @Inject constructor(
    private val nominatimApi: NominatimApiService
) {
    private val cache = mutableMapOf<String, List<AddressSuggestion>>()
    private var lastRequestTime = 0L

    suspend fun searchAddresses(query: String): List<AddressSuggestion> {
        if (query.isBlank()) return emptyList()

        cache[query]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val timeSinceLastRequest = now - lastRequestTime
                if (timeSinceLastRequest < 1000) {
                    delay(1000 - timeSinceLastRequest)
                }

                lastRequestTime = System.currentTimeMillis()
                val results = nominatimApi.searchAddresses(query)
                    .map { it.toAddressSuggestion() }

                cache[query] = results
                results
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun clearCache() {
        cache.clear()
    }
}
