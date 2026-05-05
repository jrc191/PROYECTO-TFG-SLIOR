package com.slior.data.local.entity

/**
 * Tipos de vehículos disponibles para optimización de rutas.
 * CAR: Automóvil - velocidad más rápida
 * VAN: Furgoneta - velocidad media
 * TRUCK: Camión - velocidad más lenta
 */
enum class VehicleType(val displayName: String) {
    CAR("Automóvil"),
    VAN("Furgoneta"),
    TRUCK("Camión");

    companion object {
        fun fromString(value: String?): VehicleType? {
            return try {
                valueOf(value?.uppercase() ?: return null)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
