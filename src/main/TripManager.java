package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TripManager {
    public interface Listener {
        void onStateChanged();

        void onProgress(Trip trip);

        void onPersistenceError(IOException exception);
    }

    private final AppState state;
    private final PersistenceService persistence;
    private final Listener listener;
    private final Map<Integer, Journey> runningJourneys = new ConcurrentHashMap<>();

    public TripManager(AppState state, PersistenceService persistence, Listener listener) {
        this.state = state;
        this.persistence = persistence;
        this.listener = listener;
    }

    public synchronized Trip createTrip(String origin, String destination, Vehicle vehicle) {
        if (origin == null || destination == null || origin.equals(destination)) {
            throw new IllegalArgumentException("Selecciona puntos de inicio y destino diferentes");
        }
        Route route = state.findRoute(origin, destination)
                .orElseThrow(() -> new IllegalArgumentException("No existe una ruta entre los puntos seleccionados"));
        if (vehicle == null || !vehicle.isAvailable()) {
            throw new IllegalArgumentException("Selecciona un vehículo disponible");
        }
        Driver driver = state.getDrivers().stream()
                .filter(Driver::isAvailable)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay pilotos disponibles por el momento"));

        Trip trip = state.createTrip(route, origin, destination, vehicle, driver);
        save();
        listener.onStateChanged();
        return trip;
    }

    public synchronized void startTrip(Trip trip) {
        if (trip.getStatus() != TripStatus.PREPARED || runningJourneys.containsKey(trip.getId())) {
            return;
        }
        trip.markStarted();
        trip.setStatus(TripStatus.OUTBOUND);
        launchJourney(trip, false);
        save();
        listener.onStateChanged();
    }

    public synchronized void startAll() {
        for (Trip trip : state.getTrips()) {
            if (trip.getStatus() == TripStatus.PREPARED) {
                startTrip(trip);
            }
        }
    }

    public synchronized void startReturn(Trip trip) {
        if (trip.getStatus() != TripStatus.WAITING_RETURN) {
            return;
        }
        trip.beginReturn();
        launchJourney(trip, true);
        save();
        listener.onStateChanged();
    }

    public synchronized void finishAtDestination(Trip trip) {
        if (trip.getStatus() == TripStatus.WAITING_RETURN) {
            complete(trip);
        }
    }

    public synchronized void refuel(Trip trip) {
        if (trip.getStatus() != TripStatus.OUT_OF_FUEL_OUTBOUND
                && trip.getStatus() != TripStatus.OUT_OF_FUEL_RETURN) {
            return;
        }
        state.findVehicle(trip.getVehicleId()).ifPresent(Vehicle::refuel);
        Journey journey = runningJourneys.get(trip.getId());
        if (journey != null) {
            journey.fuelAvailable();
        }
        save();
        listener.onProgress(trip);
    }

    public synchronized void restoreRunningTrips() {
        for (Trip trip : state.getTrips()) {
            TripStatus status = trip.getStatus();
            if (status == TripStatus.OUTBOUND || status == TripStatus.OUT_OF_FUEL_OUTBOUND) {
                launchJourney(trip, false);
            } else if (status == TripStatus.RETURNING || status == TripStatus.OUT_OF_FUEL_RETURN) {
                launchJourney(trip, true);
            }
        }
    }

    public synchronized void save() {
        try {
            persistence.save(state);
        } catch (IOException exception) {
            listener.onPersistenceError(exception);
        }
    }

    public synchronized void shutdown() {
        for (Journey journey : new ArrayList<>(runningJourneys.values())) {
            journey.cancel();
        }
        runningJourneys.clear();
        save();
    }

    private void launchJourney(Trip trip, boolean returnLeg) {
        if (runningJourneys.containsKey(trip.getId())) {
            return;
        }
        Vehicle vehicle = state.findVehicle(trip.getVehicleId())
                .orElseThrow(() -> new IllegalStateException("No se encontró el vehículo del viaje"));
        Journey journey = new Journey(trip, vehicle, returnLeg, new Journey.Listener() {
            @Override
            public void onProgress(Trip updatedTrip) {
                listener.onProgress(updatedTrip);
                if (((int) updatedTrip.getCurrentLegProgressKm()) % 5 == 0) {
                    save();
                }
            }

            @Override
            public void onFuelEmpty(Trip updatedTrip) {
                save();
                listener.onStateChanged();
            }

            @Override
            public void onArrived(Trip arrivedTrip, boolean arrivedFromReturn) {
                synchronized (TripManager.this) {
                    runningJourneys.remove(arrivedTrip.getId());
                    if (arrivedFromReturn) {
                        complete(arrivedTrip);
                    } else {
                        arrivedTrip.setStatus(TripStatus.WAITING_RETURN);
                        save();
                        listener.onStateChanged();
                    }
                }
            }
        });
        runningJourneys.put(trip.getId(), journey);
        journey.start();
    }

    private void complete(Trip trip) {
        synchronized (trip) {
            trip.markCompleted();
            state.releaseResources(trip);
            runningJourneys.remove(trip.getId());
            save();
        }
        listener.onStateChanged();
    }
}
