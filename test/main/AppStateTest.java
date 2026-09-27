package main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppStateTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void initializesThreeDriversAndNineFullVehicles() {
        AppState state = new AppState();

        assertEquals(3, state.getDrivers().size());
        assertEquals(9, state.getVehicles().size());
        assertTrue(state.getVehicles().stream()
                .allMatch(vehicle -> vehicle.getFuel() == vehicle.getType().getTankCapacity()));
    }

    @Test
    void assignsAndReleasesTripResources() {
        AppState state = new AppState();
        Route route = state.addRoute("Guatemala", "Antigua", 40);
        Vehicle vehicle = state.availableVehicles().getFirst();
        Driver driver = state.getDrivers().getFirst();

        Trip trip = state.createTrip(route, "Guatemala", "Antigua", vehicle, driver);

        assertFalse(vehicle.isAvailable());
        assertFalse(driver.isAvailable());
        assertEquals(2, state.availableDrivers());

        state.releaseResources(trip);
        assertTrue(vehicle.isAvailable());
        assertTrue(driver.isAvailable());
    }

    @Test
    void persistsStateInBinaryFile() throws Exception {
        Path file = temporaryDirectory.resolve("state.bin");
        PersistenceService persistence = new PersistenceService(file);
        AppState original = new AppState();
        original.addRoute("Guatemala", "Escuintla", 60);

        persistence.save(original);
        AppState restored = persistence.load();

        assertEquals(1, restored.getRoutes().size());
        assertEquals("Guatemala", restored.getRoutes().getFirst().getStart());
        assertEquals(9, restored.getVehicles().size());
    }
}
