package main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvRouteImporterTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void readsHeaderAndRoutes() throws Exception {
        Path file = temporaryDirectory.resolve("routes.csv");
        Files.writeString(file, "Inicio,Fin,Distancia\nGuatemala,Antigua,40\nAntigua,Escuintla,55\n");

        List<CsvRouteImporter.RouteData> routes = CsvRouteImporter.read(file);

        assertEquals(2, routes.size());
        assertEquals("Guatemala", routes.getFirst().start());
        assertEquals(55, routes.getLast().distance());
    }

    @Test
    void rejectsIncompleteRows() throws Exception {
        Path file = temporaryDirectory.resolve("invalid.csv");
        Files.writeString(file, "Inicio,Fin,Distancia\nGuatemala,Antigua\n");

        assertThrows(IOException.class, () -> CsvRouteImporter.read(file));
    }

    @Test
    void rejectsNonPositiveDistance() throws Exception {
        Path file = temporaryDirectory.resolve("invalid-distance.csv");
        Files.writeString(file, "Guatemala,Antigua,0\n");

        assertThrows(IOException.class, () -> CsvRouteImporter.read(file));
    }
}
