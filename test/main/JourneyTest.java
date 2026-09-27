package main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class JourneyTest {
    @Test
    void travelsRouteAndConsumesFuelProportionally() throws Exception {
        Route route = new Route(1, "A", "B", 2);
        Vehicle vehicle = new Vehicle(1, "Motocicleta 1", VehicleType.MOTORCYCLE, 1);
        Trip trip = new Trip(1, route, "A", "B", vehicle.getId(), 1);
        trip.setStatus(TripStatus.OUTBOUND);
        CountDownLatch arrived = new CountDownLatch(1);

        Journey journey = new Journey(trip, vehicle, false, listener(arrived, null));
        journey.start();

        assertTrue(arrived.await(2, TimeUnit.SECONDS));
        assertEquals(2.0, trip.getTotalDistanceKm(), 0.001);
        assertEquals(0.2, trip.getFuelConsumed(), 0.001);
        assertEquals(5.8, vehicle.getFuel(), 0.001);
    }

    @Test
    void pausesWithoutFuelAndContinuesAfterRefill() throws Exception {
        Route route = new Route(1, "A", "B", 1);
        Vehicle vehicle = new Vehicle(1, "Motocicleta 1", VehicleType.MOTORCYCLE, 1);
        assertTrue(vehicle.consume(5.95));
        Trip trip = new Trip(1, route, "A", "B", vehicle.getId(), 1);
        trip.setStatus(TripStatus.OUTBOUND);
        CountDownLatch fuelEmpty = new CountDownLatch(1);
        CountDownLatch arrived = new CountDownLatch(1);

        Journey journey = new Journey(trip, vehicle, false, listener(arrived, fuelEmpty));
        journey.start();

        assertTrue(fuelEmpty.await(1, TimeUnit.SECONDS));
        assertEquals(TripStatus.OUT_OF_FUEL_OUTBOUND, trip.getStatus());
        assertFalse(arrived.await(250, TimeUnit.MILLISECONDS));

        vehicle.refuel();
        journey.fuelAvailable();

        assertTrue(arrived.await(2, TimeUnit.SECONDS));
        assertEquals(1.0, trip.getTotalDistanceKm(), 0.001);
        assertEquals(5.9, vehicle.getFuel(), 0.001);
    }

    private static Journey.Listener listener(CountDownLatch arrived, CountDownLatch fuelEmpty) {
        return new Journey.Listener() {
            @Override
            public void onProgress(Trip trip) {
            }

            @Override
            public void onFuelEmpty(Trip trip) {
                if (fuelEmpty != null) {
                    fuelEmpty.countDown();
                }
            }

            @Override
            public void onArrived(Trip trip, boolean returnLeg) {
                arrived.countDown();
            }
        };
    }
}
