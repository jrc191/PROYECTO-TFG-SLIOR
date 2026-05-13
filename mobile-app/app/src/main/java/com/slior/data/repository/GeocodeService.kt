package com.slior.data.repository

import android.content.Context
import com.slior.data.remote.ApiService
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.util.RealAddresses
import com.slior.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.GeocoderNominatim
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodeService @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    private val cache = mutableMapOf<String, List<AddressSuggestion>>()
    private val userAgent = "SliorLogistics_TFG_App_${context.packageName}"
    
    // Geocoder de OSMDroid (BonusPack) que usa Nominatim internamente desde el cliente
    private val osmdroidGeocoder = GeocoderNominatim(Locale.getDefault(), userAgent)

    suspend fun searchAddresses(query: String): Result<List<AddressSuggestion>> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return Result.Success(emptyList())
        if (normalizedQuery.length < 3) return Result.Success(emptyList())

        cache[normalizedQuery]?.let { return Result.Success(it) }

        return withContext(Dispatchers.IO) {
            try {
                // 1. Intentar primero con el backend (Búsqueda local en DB y Caché)
                val backendResults = try {
                    apiService.searchAddresses(normalizedQuery)
                } catch (e: Exception) {
                    emptyList()
                }

                if (backendResults.isNotEmpty()) {
                    val simplified = backendResults.map { it.copy(displayName = simplifyAddress(it.displayName)) }
                    cache[normalizedQuery] = simplified
                    return@withContext Result.Success(simplified)
                }

                // 2. Si el backend no tiene nada, usar el Geocoder de OSMDroid directamente
                val osmResults = osmdroidGeocoder.getFromLocationName(normalizedQuery, 8)
                if (!osmResults.isNullOrEmpty()) {
                    val suggestions = osmResults.map { address ->
                        val displayName = address.extras.getString("display_name") ?: address.getAddressLine(0) ?: ""
                        AddressSuggestion(
                            displayName = simplifyAddress(displayName),
                            latitude = address.latitude,
                            longitude = address.longitude
                        )
                    }
                    cache[normalizedQuery] = suggestions
                    return@withContext Result.Success(suggestions)
                }

                // 3. Fallback final: Direcciones reales predefinidas (Offline/Mantenimiento)
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
                
                Result.Success(fallback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): Result<AddressSuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                // Intentar con OSMDroid directamente para el reverse geocode
                val addresses = osmdroidGeocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val displayName = address.extras.getString("display_name") ?: address.getAddressLine(0) ?: "Dirección desconocida"
                    return@withContext Result.Success(AddressSuggestion(simplifyAddress(displayName), lat, lon))
                }

                // Fallback al backend (que devolverá una genérica si no hay servicios externos)
                val result = apiService.reverseGeocode(lat, lon)
                Result.Success(result.copy(displayName = simplifyAddress(result.displayName)))
            } catch (e: Exception) {
                Result.Success(AddressSuggestion("Ubicación seleccionada ($lat, $lon)", lat, lon))
            }
        }
    }

    private fun simplifyAddress(fullAddress: String): String {
        val parts = fullAddress.split(",")
        if (parts.size <= 2) return fullAddress
        return "${parts[0].trim()}, ${parts[1].trim()}"
    }

    fun clearCache() {
        cache.clear()
    }
}
