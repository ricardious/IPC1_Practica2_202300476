package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvRouteImporter {
    private CsvRouteImporter() {
    }

    public static List<RouteData> read(Path path) throws IOException {
        List<RouteData> routes = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] values = line.split(",", -1);
                if (lineNumber == 1 && isHeader(values)) {
                    continue;
                }
                if (values.length != 3) {
                    throw new IOException("Línea " + lineNumber + ": se esperaban 3 columnas");
                }
                String start = values[0].trim();
                String end = values[1].trim();
                if (start.isEmpty() || end.isEmpty()) {
                    throw new IOException("Línea " + lineNumber + ": inicio y fin son obligatorios");
                }
                if (start.equalsIgnoreCase(end)) {
                    throw new IOException("Línea " + lineNumber + ": inicio y fin deben ser diferentes");
                }
                int distance;
                try {
                    distance = Integer.parseInt(values[2].trim());
                } catch (NumberFormatException exception) {
                    throw new IOException("Línea " + lineNumber + ": distancia inválida", exception);
                }
                if (distance <= 0) {
                    throw new IOException("Línea " + lineNumber + ": la distancia debe ser positiva");
                }
                routes.add(new RouteData(start, end, distance));
            }
        }
        if (routes.isEmpty()) {
            throw new IOException("El archivo no contiene rutas");
        }
        return routes;
    }

    private static boolean isHeader(String[] values) {
        return values.length == 3
                && values[0].trim().equalsIgnoreCase("inicio")
                && values[1].trim().equalsIgnoreCase("fin")
                && values[2].trim().equalsIgnoreCase("distancia");
    }

    public record RouteData(String start, String end, int distance) {
    }
}
