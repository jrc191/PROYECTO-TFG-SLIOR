package com.slior.model.enums;

/**
 * Tipos de vehículos disponibles para optimización de rutas.
 * Afecta la velocidad promedio y capacidad de carga.
 */
public enum VehicleType {
    CAR("Automóvil", 50, 1.2),           // 50 km/h prom., +20% por semáforos/giros
    VAN("Furgoneta", 40, 1.4),          // 40 km/h prom., +40% por paradas/carga
    TRUCK("Camión", 30, 1.6);            // 30 km/h prom., +60% por maniobras/lento

    private final String displayName;
    private final int averageSpeed;      // km/h
    private final double timeMultiplier;  // Factor multiplicador para el tiempo (realismo)

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

