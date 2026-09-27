package main;

public final class Journey extends Thread {
    public interface Listener {
        void onProgress(Trip trip);

        void onFuelEmpty(Trip trip);

        void onArrived(Trip trip, boolean returnLeg);
    }

    static final long TICK_MILLIS = 150L;
    static final double KILOMETERS_PER_TICK = 1.0;

    private final Trip trip;
    private final Vehicle vehicle;
    private final boolean returnLeg;
    private final Listener listener;
    private final Object fuelMonitor = new Object();
    private volatile boolean running = true;

    public Journey(Trip trip, Vehicle vehicle, boolean returnLeg, Listener listener) {
        super("viaje-" + trip.getId() + (returnLeg ? "-retorno" : "-ida"));
        this.trip = trip;
        this.vehicle = vehicle;
        this.returnLeg = returnLeg;
        this.listener = listener;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running && trip.getCurrentLegProgressKm() < trip.getRouteDistanceKm()) {
            double remaining = trip.getRouteDistanceKm() - trip.getCurrentLegProgressKm();
            double step = Math.min(KILOMETERS_PER_TICK, remaining);
            double consumption = step * vehicle.getType().getConsumptionPerKm();

            if (!vehicle.consume(consumption)) {
                trip.setStatus(returnLeg
                        ? TripStatus.OUT_OF_FUEL_RETURN
                        : TripStatus.OUT_OF_FUEL_OUTBOUND);
                listener.onFuelEmpty(trip);
                waitForFuel(consumption);
                if (!running) {
                    return;
                }
                trip.setStatus(returnLeg ? TripStatus.RETURNING : TripStatus.OUTBOUND);
                continue;
            }

            trip.advance(step, consumption);
            listener.onProgress(trip);
            try {
                Thread.sleep(TICK_MILLIS);
            } catch (InterruptedException exception) {
                if (!running) {
                    return;
                }
                Thread.currentThread().interrupt();
                return;
            }
        }
        if (running) {
            listener.onArrived(trip, returnLeg);
        }
    }

    private void waitForFuel(double requiredFuel) {
        synchronized (fuelMonitor) {
            while (running && vehicle.getFuel() + 0.000001 < requiredFuel) {
                try {
                    fuelMonitor.wait();
                } catch (InterruptedException exception) {
                    if (!running) {
                        return;
                    }
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void fuelAvailable() {
        synchronized (fuelMonitor) {
            fuelMonitor.notifyAll();
        }
    }

    public void cancel() {
        running = false;
        fuelAvailable();
        interrupt();
    }
}
