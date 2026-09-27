package main;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Ricardious
 */
public class Route implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String start;
    private String end;
    private int distance;

    public Route(int id, String start, String end, int distance) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

        // Por medio de esta funcion se retorna en un string los datos del objeto actual
    @Override
    public String toString() {
        return start + " → " + end + " (" + distance + " km)";
    }

    public boolean connects(String origin, String destination) {
        return (Objects.equals(start, origin) && Objects.equals(end, destination))
                || (Objects.equals(start, destination) && Objects.equals(end, origin));
    }
}
