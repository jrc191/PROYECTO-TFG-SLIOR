package com.slior.data.remote.dto

data class AddressSuggestion(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

data class NominatimSearchResult(
    val place_id: Long,
    val display_name: String,
    val lat: String,
    val lon: String,
    val type: String
) {
    fun toAddressSuggestion() = AddressSuggestion(
        displayName = display_name,
        latitude = lat.toDouble(),
        longitude = lon.toDouble()
    )
}
