package main;

import java.io.Serializable;

public enum TripStatus implements Serializable {
    PREPARED("Preparado"),
    OUTBOUND("En viaje de ida"),
    OUT_OF_FUEL_OUTBOUND("Sin combustible (ida)"),
    WAITING_RETURN("En destino"),
    RETURNING("Regresando"),
    OUT_OF_FUEL_RETURN("Sin combustible (retorno)"),
    COMPLETED("Completado");

    private final String displayName;

    TripStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTravelling() {
        return this == OUTBOUND || this == RETURNING
                || this == OUT_OF_FUEL_OUTBOUND || this == OUT_OF_FUEL_RETURN;
    }
}
