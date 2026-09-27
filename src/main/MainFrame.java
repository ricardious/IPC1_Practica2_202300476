package main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private static final Color PRIMARY = new Color(67, 56, 202);
    private static final Color PRIMARY_DARK = new Color(49, 46, 129);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DecimalFormat DECIMAL = new DecimalFormat("0.00");

    private final AppState state;
    private final PersistenceService persistence;
    private final TripManager tripManager;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<Integer, TripCard> tripCards = new ConcurrentHashMap<>();

    private final DefaultTableModel routesModel = readOnlyModel(
            "ID", "Inicio", "Fin", "Distancia (km)");
    private final DefaultTableModel historyModel = readOnlyModel(
            "Viaje", "Ruta", "Inicio", "Fin", "Vehículo", "Piloto",
            "Distancia ruta (km)", "Trayectoria (km)", "Combustible (gal)");
    private final JTable routesTable = new JTable(routesModel);
    private final JTable historyTable = new JTable(historyModel);
    private final JComboBox<String> originBox = new JComboBox<>();
    private final JComboBox<String> destinationBox = new JComboBox<>();
    private final JComboBox<Vehicle> vehicleBox = new JComboBox<>();
    private final JLabel pilotAvailability = new JLabel();
    private final JButton generateButton = primaryButton("Generar viaje");
    private final JPanel tripsContainer = new JPanel();
    private final JLabel storageLabel = new JLabel();
    private boolean persistenceErrorVisible;

    public MainFrame(AppState state, PersistenceService persistence) {
        super("UDrive - Gestión de viajes");
        this.state = state;
        this.persistence = persistence;
        this.tripManager = new TripManager(state, persistence, new TripManager.Listener() {
            @Override
            public void onStateChanged() {
                SwingUtilities.invokeLater(MainFrame.this::refreshAll);
            }

            @Override
            public void onProgress(Trip trip) {
                SwingUtilities.invokeLater(() -> refreshTripProgress(trip));
            }

            @Override
            public void onPersistenceError(IOException exception) {
                SwingUtilities.invokeLater(() -> showPersistenceError(exception));
            }
        });

        configureFrame();
        buildInterface();
        refreshAll();
        tripManager.restoreRunningTrips();
    }

    private void configureFrame() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                tripManager.shutdown();
                dispose();
            }
        });
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);

        contentPanel.add(createRoutesPanel(), "routes");
        contentPanel.add(createGeneratePanel(), "generate");
        contentPanel.add(createTripsPanel(), "trips");
        contentPanel.add(createHistoryPanel(), "history");
        cardLayout.show(contentPanel, "routes");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel(loadScaledIcon("/vehicles/logo.png", 74, 74));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel("UDRIVE");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 27f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setMaximumSize(new Dimension(188, 38));
        JLabel subtitle = new JLabel("Gestión de viajes");
        subtitle.setForeground(new Color(199, 210, 254));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setMaximumSize(new Dimension(188, 24));

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(title);
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(40));
        sidebar.add(navigationButton("Rutas", "routes"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(navigationButton("Generar viaje", "generate"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(navigationButton("Viajes en curso", "trips"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(navigationButton("Historial", "history"));
        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("UDrive 1.0");
        version.setForeground(new Color(165, 180, 252));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(version);
        return sidebar;
    }

    private JButton navigationButton(String text, String card) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMinimumSize(new Dimension(188, 46));
        button.setPreferredSize(new Dimension(188, 46));
        button.setMaximumSize(new Dimension(188, 46));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.addActionListener(event -> {
            refreshAll();
            cardLayout.show(contentPanel, card);
        });
        return button;
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        status.setBackground(Color.WHITE);
        storageLabel.setForeground(MUTED);
        storageLabel.setText("Persistencia automática activa");
        status.add(storageLabel);
        return status;
    }

    private JPanel createRoutesPanel() {
        JPanel panel = pagePanel("Rutas disponibles",
                "Carga archivos CSV con la estructura Inicio,Fin,Distancia y administra sus valores.");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton loadButton = primaryButton("Cargar rutas CSV");
        JButton editButton = secondaryButton("Editar distancia");
        loadButton.addActionListener(event -> chooseCsv());
        editButton.addActionListener(event -> editSelectedRoute());
        actions.add(loadButton);
        actions.add(editButton);
        panel.add(actions, BorderLayout.NORTH);

        routesTable.setRowHeight(30);
        routesTable.setFillsViewportHeight(true);
        routesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(routesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGeneratePanel() {
        JPanel panel = pagePanel("Generar viaje",
                "Selecciona una ruta y uno de los nueve vehículos. El primer piloto libre será asignado.");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addField(form, constraints, 0, "Punto inicial", originBox);
        addField(form, constraints, 1, "Punto final", destinationBox);
        addField(form, constraints, 2, "Vehículo", vehicleBox);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        pilotAvailability.setFont(pilotAvailability.getFont().deriveFont(Font.BOLD));
        form.add(pilotAvailability, constraints);

        constraints.gridy = 4;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        generateButton.addActionListener(event -> generateTrip());
        form.add(generateButton, constraints);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(24, 0, 120, 0));
        center.add(form, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel form, GridBagConstraints constraints, int row,
                          String labelText, Component field) {
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.weightx = 0.25;
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        form.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0.75;
        form.add(field, constraints);
    }

    private JPanel createTripsPanel() {
        JPanel panel = pagePanel("Viajes preparados y en curso",
                "Inicia cada viaje por separado o todos a la vez. El retorno se autoriza manualmente.");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton startAll = primaryButton("Iniciar todos los preparados");
        startAll.addActionListener(event -> tripManager.startAll());
        actions.add(startAll);
        panel.add(actions, BorderLayout.NORTH);

        tripsContainer.setLayout(new BoxLayout(tripsContainer, BoxLayout.Y_AXIS));
        tripsContainer.setBackground(SURFACE);
        JScrollPane scroll = new JScrollPane(tripsContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = pagePanel("Historial de viajes",
                "Registro persistente de los viajes completados y su consumo de combustible.");
        historyTable.setRowHeight(30);
        historyTable.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel pagePanel(String titleText, String subtitleText) {
        return new PagePanel(titleText, subtitleText);
    }

    private void chooseCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo de rutas");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            List<CsvRouteImporter.RouteData> imported = CsvRouteImporter.read(file.toPath());
            int added = 0;
            int duplicates = 0;
            for (CsvRouteImporter.RouteData route : imported) {
                if (state.findRoute(route.start(), route.end()).isPresent()) {
                    duplicates++;
                } else {
                    state.addRoute(route.start(), route.end(), route.distance());
                    added++;
                }
            }
            tripManager.save();
            refreshAll();
            JOptionPane.showMessageDialog(this,
                    "Se agregaron " + added + " rutas. Duplicadas omitidas: " + duplicates + ".",
                    "Carga completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            showError("No se pudo cargar el CSV", exception.getMessage());
        }
    }

    private void editSelectedRoute() {
        int row = routesTable.getSelectedRow();
        if (row < 0) {
            showError("Selecciona una ruta", "Debes seleccionar una fila antes de editarla.");
            return;
        }
        int modelRow = routesTable.convertRowIndexToModel(row);
        int routeId = (int) routesModel.getValueAt(modelRow, 0);
        Route route = state.getRoutes().stream()
                .filter(candidate -> candidate.getId() == routeId)
                .findFirst().orElse(null);
        if (route == null) {
            return;
        }
        String value = JOptionPane.showInputDialog(this,
                "Nueva distancia para " + route.getStart() + " → " + route.getEnd() + ":",
                route.getDistance());
        if (value == null) {
            return;
        }
        try {
            int distance = Integer.parseInt(value.trim());
            if (distance <= 0) {
                throw new NumberFormatException();
            }
            route.setDistance(distance);
            tripManager.save();
            refreshAll();
        } catch (NumberFormatException exception) {
            showError("Distancia inválida", "Ingresa un número entero mayor que cero.");
        }
    }

    private void generateTrip() {
        try {
            tripManager.createTrip((String) originBox.getSelectedItem(),
                    (String) destinationBox.getSelectedItem(),
                    (Vehicle) vehicleBox.getSelectedItem());
            cardLayout.show(contentPanel, "trips");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError("No se pudo generar el viaje", exception.getMessage());
        }
    }

    private void refreshAll() {
        refreshRoutes();
        refreshGenerator();
        refreshTrips();
        refreshHistory();
    }

    private void refreshRoutes() {
        routesModel.setRowCount(0);
        for (Route route : state.getRoutes()) {
            routesModel.addRow(new Object[]{route.getId(), route.getStart(), route.getEnd(), route.getDistance()});
        }
    }

    private void refreshGenerator() {
        String selectedOrigin = (String) originBox.getSelectedItem();
        String selectedDestination = (String) destinationBox.getSelectedItem();
        Set<String> locations = new LinkedHashSet<>();
        state.getRoutes().stream()
                .sorted(Comparator.comparing(Route::getStart, String.CASE_INSENSITIVE_ORDER))
                .forEach(route -> {
                    locations.add(route.getStart());
                    locations.add(route.getEnd());
                });
        originBox.setModel(new DefaultComboBoxModel<>(locations.toArray(String[]::new)));
        destinationBox.setModel(new DefaultComboBoxModel<>(locations.toArray(String[]::new)));
        selectIfPresent(originBox, selectedOrigin);
        selectIfPresent(destinationBox, selectedDestination);
        if (destinationBox.getItemCount() > 1 && destinationBox.getSelectedIndex() == 0) {
            destinationBox.setSelectedIndex(1);
        }

        Integer selectedVehicleId = vehicleBox.getSelectedItem() instanceof Vehicle vehicle
                ? vehicle.getId() : null;
        List<Vehicle> available = state.availableVehicles();
        vehicleBox.setModel(new DefaultComboBoxModel<>(available.toArray(Vehicle[]::new)));
        if (selectedVehicleId != null) {
            for (int index = 0; index < vehicleBox.getItemCount(); index++) {
                if (vehicleBox.getItemAt(index).getId() == selectedVehicleId) {
                    vehicleBox.setSelectedIndex(index);
                    break;
                }
            }
        }

        long availableDrivers = state.availableDrivers();
        boolean hasDrivers = availableDrivers > 0;
        pilotAvailability.setText(hasDrivers
                ? "Pilotos disponibles: " + availableDrivers + " de 3"
                : "No hay pilotos disponibles por el momento");
        pilotAvailability.setForeground(hasDrivers ? SUCCESS : DANGER);
        generateButton.setEnabled(hasDrivers && vehicleBox.getItemCount() > 0
                && originBox.getItemCount() > 0);
    }

    private void refreshTrips() {
        tripsContainer.removeAll();
        tripCards.clear();
        List<Trip> activeTrips = state.getTrips().stream()
                .filter(Trip::isActive)
                .sorted(Comparator.comparingInt(Trip::getId))
                .toList();
        if (activeTrips.isEmpty()) {
            JLabel empty = new JLabel("No hay viajes preparados o en curso.", SwingConstants.CENTER);
            empty.setForeground(MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(80, 20, 80, 20));
            tripsContainer.add(empty);
        } else {
            for (Trip trip : activeTrips) {
                TripCard card = new TripCard(trip);
                tripCards.put(trip.getId(), card);
                tripsContainer.add(card);
                tripsContainer.add(Box.createVerticalStrut(12));
            }
        }
        tripsContainer.revalidate();
        tripsContainer.repaint();
    }

    private void refreshTripProgress(Trip trip) {
        TripCard card = tripCards.get(trip.getId());
        if (card != null) {
            card.refresh();
        } else {
            refreshTrips();
        }
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);
        state.getTrips().stream()
                .filter(trip -> trip.getStatus() == TripStatus.COMPLETED)
                .sorted(Comparator.comparingInt(Trip::getId).reversed())
                .forEach(trip -> {
                    Vehicle vehicle = state.findVehicle(trip.getVehicleId()).orElse(null);
                    Driver driver = state.findDriver(trip.getDriverId()).orElse(null);
                    historyModel.addRow(new Object[]{
                            trip.getId(), trip.getRouteId(), formatDate(trip.getStartedAt()),
                            formatDate(trip.getEndedAt()),
                            vehicle == null ? "-" : vehicle.getName(),
                            driver == null ? "-" : driver.getName(),
                            trip.getRouteDistanceKm(),
                            DECIMAL.format(trip.getTotalDistanceKm()),
                            DECIMAL.format(trip.getFuelConsumed())
                    });
                });
    }

    private void showPersistenceError(IOException exception) {
        storageLabel.setForeground(DANGER);
        storageLabel.setText("Error al guardar: " + exception.getMessage());
        if (!persistenceErrorVisible) {
            persistenceErrorVisible = true;
            showError("No se pudo guardar el estado", exception.getMessage());
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private static String formatDate(LocalDateTime date) {
        return date == null ? "-" : DATE_TIME.format(date);
    }

    private static void selectIfPresent(JComboBox<String> box, String value) {
        if (value == null) {
            return;
        }
        for (int index = 0; index < box.getItemCount(); index++) {
            if (value.equals(box.getItemAt(index))) {
                box.setSelectedIndex(index);
                return;
            }
        }
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setFocusPainted(false);
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setFocusPainted(false);
        return button;
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static ImageIcon loadScaledIcon(String path, int width, int height) {
        java.net.URL resource = MainFrame.class.getResource(path);
        if (resource == null) {
            return null;
        }
        Image image = new ImageIcon(resource).getImage()
                .getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private final class TripCard extends JPanel {
        private final Trip trip;
        private final JLabel routeLabel = new JLabel();
        private final JLabel statusLabel = new JLabel();
        private final JLabel vehicleLabel = new JLabel();
        private final JLabel driverLabel = new JLabel();
        private final JLabel fuelLabel = new JLabel();
        private final JProgressBar routeProgress = new JProgressBar(0, 100);
        private final JProgressBar fuelProgress = new JProgressBar(0, 100);
        private final JButton startButton = primaryButton("Iniciar");
        private final JButton returnButton = secondaryButton("Iniciar retorno");
        private final JButton finishButton = secondaryButton("Finalizar aquí");
        private final JButton refuelButton = primaryButton("Recargar combustible");

        private TripCard(Trip trip) {
            this.trip = trip;
            setLayout(new BorderLayout(18, 8));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225)),
                    BorderFactory.createEmptyBorder(15, 16, 15, 16)));

            Vehicle vehicle = state.findVehicle(trip.getVehicleId()).orElse(null);
            JLabel icon = new JLabel();
            icon.setPreferredSize(new Dimension(110, 80));
            if (vehicle != null) {
                String path = "/vehicles/" + vehicle.getType().getIconPrefix()
                        + "_" + vehicle.getUnitNumber() + ".gif";
                icon.setIcon(loadScaledIcon(path, 100, 65));
            }
            add(icon, BorderLayout.WEST);

            JPanel details = new JPanel();
            details.setOpaque(false);
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
            routeLabel.setFont(routeLabel.getFont().deriveFont(Font.BOLD, 16f));
            statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
            routeProgress.setStringPainted(true);
            fuelProgress.setStringPainted(true);
            details.add(routeLabel);
            details.add(Box.createVerticalStrut(5));
            details.add(statusLabel);
            details.add(vehicleLabel);
            details.add(driverLabel);
            details.add(Box.createVerticalStrut(7));
            details.add(routeProgress);
            details.add(Box.createVerticalStrut(5));
            details.add(fuelLabel);
            details.add(fuelProgress);
            add(details, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new GridLayout(4, 1, 6, 6));
            buttons.setOpaque(false);
            startButton.addActionListener(event -> tripManager.startTrip(trip));
            returnButton.addActionListener(event -> tripManager.startReturn(trip));
            finishButton.addActionListener(event -> tripManager.finishAtDestination(trip));
            refuelButton.addActionListener(event -> tripManager.refuel(trip));
            buttons.add(startButton);
            buttons.add(returnButton);
            buttons.add(finishButton);
            buttons.add(refuelButton);
            add(buttons, BorderLayout.EAST);
            refresh();
        }

        private void refresh() {
            TripStatus status = trip.getStatus();
            boolean returning = status == TripStatus.RETURNING
                    || status == TripStatus.OUT_OF_FUEL_RETURN;
            routeLabel.setText("Viaje #" + trip.getId() + " • "
                    + (returning
                    ? trip.getDestination() + " → " + trip.getOrigin() + " (retorno)"
                    : trip.getOrigin() + " → " + trip.getDestination()));
            statusLabel.setText(status.getDisplayName());
            statusLabel.setForeground(status == TripStatus.OUT_OF_FUEL_OUTBOUND
                    || status == TripStatus.OUT_OF_FUEL_RETURN ? DANGER
                    : returning ? PRIMARY : SUCCESS);

            Vehicle vehicle = state.findVehicle(trip.getVehicleId()).orElse(null);
            Driver driver = state.findDriver(trip.getDriverId()).orElse(null);
            vehicleLabel.setText("Vehículo: " + (vehicle == null ? "-" : vehicle.getName()));
            driverLabel.setText("Piloto: " + (driver == null ? "-" : driver.getName()));

            int routePercent = trip.getProgressPercentage();
            routeProgress.setValue(routePercent);
            routeProgress.setString(DECIMAL.format(trip.getCurrentLegProgressKm())
                    + " / " + trip.getRouteDistanceKm() + " km");
            if (vehicle != null) {
                int fuelPercent = (int) Math.round(vehicle.fuelPercentage());
                fuelProgress.setValue(fuelPercent);
                fuelProgress.setString(fuelPercent + "%");
                fuelLabel.setText("Combustible: " + DECIMAL.format(vehicle.getFuel())
                        + " / " + DECIMAL.format(vehicle.getType().getTankCapacity()) + " gal");
            }

            startButton.setEnabled(status == TripStatus.PREPARED);
            returnButton.setEnabled(status == TripStatus.WAITING_RETURN);
            finishButton.setEnabled(status == TripStatus.WAITING_RETURN);
            refuelButton.setEnabled(status == TripStatus.OUT_OF_FUEL_OUTBOUND
                    || status == TripStatus.OUT_OF_FUEL_RETURN);
        }
    }

    private static final class PagePanel extends JPanel {
        private final JPanel body;

        private PagePanel(String titleText, String subtitleText) {
            super(new BorderLayout(0, 16));
            setBackground(SURFACE);
            setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));

            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(titleText);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
            JLabel subtitle = new JLabel(subtitleText);
            subtitle.setForeground(MUTED);
            subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
            header.add(title);
            header.add(subtitle);
            super.add(header, BorderLayout.PAGE_START);

            body = new JPanel(new BorderLayout());
            body.setOpaque(false);
            super.add(body, BorderLayout.CENTER);
        }

        @Override
        public Component add(Component component) {
            return body.add(component);
        }

        @Override
        public void add(Component component, Object constraints) {
            body.add(component, constraints);
        }
    }
}
