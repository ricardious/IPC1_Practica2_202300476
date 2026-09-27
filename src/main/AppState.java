package main;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Route> routes = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Driver> drivers = new ArrayList<>();
    private final List<Trip> trips = new ArrayList<>();
    private int nextRouteId = 1;
    private int nextTripId = 1;

    public AppState() {
        initializeFleet();
    }

    private void initializeFleet() {
        if (!drivers.isEmpty() || !vehicles.isEmpty()) {
            return;
        }
        for (int index = 1; index <= 3; index++) {
            drivers.add(new Driver(index, "Piloto " + index));
        }

        int id = 1;
        for (VehicleType type : VehicleType.values()) {
            for (int unit = 1; unit <= 3; unit++) {
                vehicles.add(new Vehicle(id++, type.getDisplayName() + " " + unit, type, unit));
            }
        }
    }

    public synchronized List<Route> getRoutes() {
        return new ArrayList<>(routes);
    }

    public synchronized List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    public synchronized List<Driver> getDrivers() {
        return new ArrayList<>(drivers);
    }

    public synchronized List<Trip> getTrips() {
        return new ArrayList<>(trips);
    }

    public synchronized Route addRoute(String start, String end, int distance) {
        Route route = new Route(nextRouteId++, start, end, distance);
        routes.add(route);
        return route;
    }

    public synchronized void addRoute(Route route) {
        routes.add(route);
        nextRouteId = Math.max(nextRouteId, route.getId() + 1);
    }

    public synchronized Optional<Route> findRoute(String origin, String destination) {
        return routes.stream().filter(route -> route.connects(origin, destination)).findFirst();
    }

    public synchronized Optional<Vehicle> findVehicle(int id) {
        return vehicles.stream().filter(vehicle -> vehicle.getId() == id).findFirst();
    }

    public synchronized Optional<Driver> findDriver(int id) {
        return drivers.stream().filter(driver -> driver.getId() == id).findFirst();
    }

    public synchronized Optional<Trip> findTrip(int id) {
        return trips.stream().filter(trip -> trip.getId() == id).findFirst();
    }

    public synchronized Trip createTrip(Route route, String origin, String destination,
                                        Vehicle vehicle, Driver driver) {
        int tripId = nextTripId++;
        vehicle.assignTo(tripId);
        driver.assignTo(tripId);
        Trip trip = new Trip(tripId, route, origin, destination, vehicle.getId(), driver.getId());
        trips.add(trip);
        return trip;
    }

    public synchronized long availableDrivers() {
        return drivers.stream().filter(Driver::isAvailable).count();
    }

    public synchronized List<Vehicle> availableVehicles() {
        return vehicles.stream().filter(Vehicle::isAvailable).toList();
    }

    public synchronized void releaseResources(Trip trip) {
        findVehicle(trip.getVehicleId()).ifPresent(Vehicle::release);
        findDriver(trip.getDriverId()).ifPresent(Driver::release);
    }
}
