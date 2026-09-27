package main;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.EventQueue;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        FlatMacLightLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);

        EventQueue.invokeLater(() -> {
            PersistenceService persistence = new PersistenceService(
                    Path.of("data", "udrive-state.bin"));
            AppState state;
            try {
                state = persistence.load();
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo recuperar el estado anterior. Se iniciará una sesión nueva.\n"
                                + exception.getMessage(),
                        "Estado dañado", JOptionPane.WARNING_MESSAGE);
                state = new AppState();
            }
            MainFrame frame = new MainFrame(state, persistence);
            frame.setVisible(true);
        });
    }
}
