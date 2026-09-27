package main;

import java.io.Serial;
import java.io.Serializable;

public class Vehicle implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final VehicleType type;
    private final int unitNumber;
    private double fuel;
    private Integer assignedTripId;

    public Vehicle(int id, String name, VehicleType type, int unitNumber) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.unitNumber = unitNumber;
        this.fuel = type.getTankCapacity();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VehicleType getType() {
        return type;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public synchronized double getFuel() {
        return fuel;
    }

    public synchronized Integer getAssignedTripId() {
        return assignedTripId;
    }

    public synchronized boolean isAvailable() {
        return assignedTripId == null;
    }

    public synchronized void assignTo(int tripId) {
        if (!isAvailable()) {
            throw new IllegalStateException("El vehículo ya tiene un viaje asignado");
        }
        assignedTripId = tripId;
    }

    public synchronized void release() {
        assignedTripId = null;
    }

    public synchronized boolean consume(double gallons) {
        if (gallons < 0) {
            throw new IllegalArgumentException("El consumo no puede ser negativo");
        }
        if (fuel + 0.000001 < gallons) {
            return false;
        }
        fuel = Math.max(0, fuel - gallons);
        return true;
    }

    public synchronized void refuel() {
        fuel = type.getTankCapacity();
    }

    public double fuelPercentage() {
        return (getFuel() / type.getTankCapacity()) * 100.0;
    }

    @Override
    public String toString() {
        return name;
    }
}
