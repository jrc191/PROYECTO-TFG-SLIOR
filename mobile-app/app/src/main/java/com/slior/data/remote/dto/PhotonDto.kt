package com.slior.data.remote.dto

data class PhotonResponse(
    val features: List<PhotonFeature>? = null
)

data class PhotonFeature(
    val properties: PhotonProperties? = null,
    val geometry: PhotonGeometry? = null
) {
    fun toAddressSuggestionOrNull(): AddressSuggestion? {
        val coords = geometry?.coordinates
        if (coords == null || coords.size < 2) return null

        val lon = coords[0]
        val lat = coords[1]
        val name = properties?.name?.trim()
        val city = properties?.city?.trim()
        val country = properties?.country?.trim()

        val display = listOfNotNull(name, city, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { null }
            ?: return null

        return AddressSuggestion(
            displayName = display,
            latitude = lat,
            longitude = lon
        )
    }
}

data class PhotonProperties(
    val name: String? = null,
    val city: String? = null,
    val country: String? = null
)

data class PhotonGeometry(
    val coordinates: List<Double>? = null
)

