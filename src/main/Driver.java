package main;

import java.io.Serial;
import java.io.Serializable;

public class Driver implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private Integer assignedTripId;

    public Driver(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public synchronized Integer getAssignedTripId() {
        return assignedTripId;
    }

    public synchronized boolean isAvailable() {
        return assignedTripId == null;
    }

    public synchronized void assignTo(int tripId) {
        if (!isAvailable()) {
            throw new IllegalStateException("El piloto ya tiene un viaje asignado");
        }
        assignedTripId = tripId;
    }

    public synchronized void release() {
        assignedTripId = null;
    }
}
