package main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TripManagerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void completesTripAndReleasesDriverAndVehicle() throws Exception {
        AppState state = new AppState();
        Route route = state.addRoute("A", "B", 1);
        Vehicle vehicle = state.availableVehicles().getFirst();
        Driver driver = state.getDrivers().getFirst();
        PersistenceService persistence = new PersistenceService(
                temporaryDirectory.resolve("state.bin"));
        TripManager manager = new TripManager(state, persistence, silentListener());
        Trip trip = manager.createTrip("A", "B", vehicle);

        manager.startTrip(trip);
        waitUntil(() -> trip.getStatus() == TripStatus.WAITING_RETURN);
        manager.finishAtDestination(trip);

        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertTrue(vehicle.isAvailable());
        assertTrue(driver.isAvailable());
        assertEquals(1.0, trip.getTotalDistanceKm(), 0.001);
        assertEquals(0.1, trip.getFuelConsumed(), 0.001);
        assertEquals(1, persistence.load().getTrips().size());
    }

    @Test
    void returnTripTravelsBothDirections() throws Exception {
        AppState state = new AppState();
        state.addRoute("A", "B", 1);
        Vehicle vehicle = state.availableVehicles().getFirst();
        PersistenceService persistence = new PersistenceService(
                temporaryDirectory.resolve("return-state.bin"));
        TripManager manager = new TripManager(state, persistence, silentListener());
        Trip trip = manager.createTrip("A", "B", vehicle);

        manager.startTrip(trip);
        waitUntil(() -> trip.getStatus() == TripStatus.WAITING_RETURN);
        manager.startReturn(trip);
        waitUntil(() -> trip.getStatus() == TripStatus.COMPLETED);

        assertEquals(2.0, trip.getTotalDistanceKm(), 0.001);
        assertEquals(0.2, trip.getFuelConsumed(), 0.001);
        assertTrue(vehicle.isAvailable());
    }

    private static void waitUntil(Check condition) throws Exception {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(3));
        while (!condition.isTrue() && Instant.now().isBefore(deadline)) {
            Thread.sleep(20);
        }
        assertTrue(condition.isTrue(), "La operación no terminó dentro del tiempo esperado");
    }

    private static TripManager.Listener silentListener() {
        return new TripManager.Listener() {
            @Override
            public void onStateChanged() {
            }

            @Override
            public void onProgress(Trip trip) {
            }

            @Override
            public void onPersistenceError(IOException exception) {
                throw new AssertionError(exception);
            }
        };
    }

    @FunctionalInterface
    private interface Check {
        boolean isTrue();
    }
}
