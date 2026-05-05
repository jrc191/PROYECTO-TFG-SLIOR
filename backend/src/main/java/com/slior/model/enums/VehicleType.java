package com.slior.model.enums;

/**
 * Tipos de vehículos disponibles para optimización de rutas.
 * Afecta la velocidad promedio y capacidad de carga.
 */
public enum VehicleType {
    CAR("Automóvil", 80, 0.5),           // 80 km/h, factor multiplicador 0.5 (rápido)
    VAN("Furgoneta", 70, 0.75),          // 70 km/h, factor 0.75 (medio)
    TRUCK("Camión", 60, 1.0);            // 60 km/h, factor 1.0 (lento)

    private final String displayName;
    private final int averageSpeed;      // km/h
    private final double timeMultiplier;  // Factor multiplicador para el tiempo

    VehicleType(String displayName, int averageSpeed, double timeMultiplier) {
        this.displayName = displayName;
        this.averageSpeed = averageSpeed;
        this.timeMultiplier = timeMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAverageSpeed() {
        return averageSpeed;
    }

    public double getTimeMultiplier() {
        return timeMultiplier;
    }
}
