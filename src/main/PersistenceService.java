package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PersistenceService {
    private final Path stateFile;

    public PersistenceService(Path stateFile) {
        this.stateFile = stateFile;
    }

    public synchronized AppState load() throws IOException {
        if (!Files.exists(stateFile)) {
            return new AppState();
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(stateFile))) {
            Object stored = input.readObject();
            if (!(stored instanceof AppState appState)) {
                throw new IOException("El archivo no contiene un estado válido");
            }
            return appState;
        } catch (ClassNotFoundException | ClassCastException exception) {
            throw new IOException("No se pudo interpretar el estado guardado", exception);
        }
    }

    public synchronized void save(AppState state) throws IOException {
        Path parent = stateFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = stateFile.resolveSibling(stateFile.getFileName() + ".tmp");
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(temporary))) {
            output.writeObject(state);
        }
        try {
            Files.move(temporary, stateFile, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, stateFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Path getStateFile() {
        return stateFile;
    }
}
