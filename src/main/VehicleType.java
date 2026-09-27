package main;

import java.io.Serializable;

public enum VehicleType implements Serializable {
    MOTORCYCLE("Motocicleta", 0.10, 6.0, "motorcycle"),
    STANDARD("Vehículo estándar", 0.30, 10.0, "standard_vehicle"),
    PREMIUM("Vehículo premium", 0.45, 12.0, "premium_vehicle");

    private final String displayName;
    private final double consumptionPerKm;
    private final double tankCapacity;
    private final String iconPrefix;

    VehicleType(String displayName, double consumptionPerKm, double tankCapacity, String iconPrefix) {
        this.displayName = displayName;
        this.consumptionPerKm = consumptionPerKm;
        this.tankCapacity = tankCapacity;
        this.iconPrefix = iconPrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getConsumptionPerKm() {
        return consumptionPerKm;
    }

    public double getTankCapacity() {
        return tankCapacity;
    }

    public String getIconPrefix() {
        return iconPrefix;
    }
}
