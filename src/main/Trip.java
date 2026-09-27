package main;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Trip implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final int routeId;
    private final String origin;
    private final String destination;
    private final int routeDistanceKm;
    private final int vehicleId;
    private final int driverId;
    private final LocalDateTime createdAt;
    private TripStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private double currentLegProgressKm;
    private double totalDistanceKm;
    private double fuelConsumed;

    public Trip(int id, Route route, String origin, String destination, int vehicleId, int driverId) {
        this.id = id;
        this.routeId = route.getId();
        this.origin = origin;
        this.destination = destination;
        this.routeDistanceKm = route.getDistance();
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.createdAt = LocalDateTime.now();
        this.status = TripStatus.PREPARED;
    }

    public int getId() {
        return id;
    }

    public int getRouteId() {
        return routeId;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public int getRouteDistanceKm() {
        return routeDistanceKm;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public int getDriverId() {
        return driverId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public synchronized TripStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(TripStatus status) {
        this.status = status;
    }

    public synchronized LocalDateTime getStartedAt() {
        return startedAt;
    }

    public synchronized void markStarted() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    public synchronized LocalDateTime getEndedAt() {
        return endedAt;
    }

    public synchronized void markCompleted() {
        status = TripStatus.COMPLETED;
        endedAt = LocalDateTime.now();
    }

    public synchronized double getCurrentLegProgressKm() {
        return currentLegProgressKm;
    }

    public synchronized double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public synchronized double getFuelConsumed() {
        return fuelConsumed;
    }

    public synchronized void advance(double distanceKm, double consumedFuel) {
        currentLegProgressKm = Math.min(routeDistanceKm, currentLegProgressKm + distanceKm);
        totalDistanceKm += distanceKm;
        fuelConsumed += consumedFuel;
    }

    public synchronized void beginReturn() {
        currentLegProgressKm = 0;
        status = TripStatus.RETURNING;
    }

    public synchronized int getProgressPercentage() {
        if (routeDistanceKm <= 0) {
            return 0;
        }
        return (int) Math.round((currentLegProgressKm / routeDistanceKm) * 100.0);
    }

    public boolean isActive() {
        return getStatus() != TripStatus.COMPLETED;
    }
}
