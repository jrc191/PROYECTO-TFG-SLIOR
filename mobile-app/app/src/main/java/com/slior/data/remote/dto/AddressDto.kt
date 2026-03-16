package com.slior.data.remote.dto

data class AddressSuggestion(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

data class NominatimSearchResult(
    val place_id: Long? = null,
    val display_name: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val type: String? = null
) {
    fun toAddressSuggestionOrNull(): AddressSuggestion? {
        val safeName = display_name?.trim()
        val safeLat = lat?.toDoubleOrNull()
        val safeLon = lon?.toDoubleOrNull()
        if (safeName.isNullOrBlank() || safeLat == null || safeLon == null) return null

        return AddressSuggestion(
            displayName = safeName,
            latitude = safeLat,
            longitude = safeLon
        )
    }
}
