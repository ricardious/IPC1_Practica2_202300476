package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import main.MainFrame.jPanelGradient;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import org.netbeans.lib.awtextra.AbsoluteLayout;

/**
 *
 * @author Ricardious
 */
public class MainFrame extends javax.swing.JFrame {
    // Mantener un registro de los pilotos y sus viajes asignados

    private HashMap<String, Boolean> pilotsAvailability = new HashMap<>();

    // Variable para almacenar la disponibilidad de los pilotos
    private boolean[] pilotsAvailable = {true, true, true}; // Suponiendo que hay 3 pilotos

    // Variables to store data for the first trip
    private String startLocation1;
    private String endLocation1;
    private String transportType1;
    private int distance1;

// Variables to store data for the second trip
    private String startLocation2;
    private String endLocation2;
    private String transportType2;
    private int distance2;

// Variables to store data for the third trip
    private String startLocation3;
    private String endLocation3;
    private String transportType3;
    private int distance3;

    private int tripsGenerated = 0;

    private int mouseX, mouseY;
    private JFileChooser fileChooser;
    private File JFileSelected;
    private Rectangle transport;
    
    
    public JLabel vehicle1 = new JLabel();
    public JLabel vehicle2 = new JLabel();
    public JLabel vehicle3 = new JLabel();

    public MainFrame() {
        this.setUndecorated(true);
        initComponents();
        this.setLocationRelativeTo(null);
        // Configuración inicial del JLabel de pilotos no disponibles
        noPilotsLabel.setForeground(Color.RED); // Texto en color rojo
        noPilotsLabel.setText("No hay pilotos disponibles por el momento");
        noPilotsLabel.setVisible(false); // Inicialmente invisible

        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20);
        setShape(shape);

        // Agregar listener para arrastrar la ventana
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                mouseX = evt.getX();
                mouseY = evt.getY();
            }
        });
        this.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent evt) {
                // Obtener la posición actual del mouse en la pantalla
                int x = evt.getXOnScreen();
                int y = evt.getYOnScreen();

                // Calcular la nueva posición de la ventana
                setLocation(x - mouseX, y - mouseY);
            }
        });

        panelTripStart.repaint();
         panelTripStart.setFocusable(true);
    }
    
    public JLabel getTransport1(){
        return lblTransport1;
    }
    
    public JLabel getBarrera(){
        return lblBarrera;
    }

    public void refreshRoutesTable() {
        DefaultTableModel DT = new DefaultTableModel(new String[]{"ID", "Inicio", "Final", "Distancia"}, Main.routes.size());
        jTable1.setModel(DT);
        TableModel tbModel = jTable1.getModel();

        for (int i = 0; i < Main.routes.size(); i++) {
            Route ruta = Main.routes.get(i);
            tbModel.setValueAt(ruta.getId(), i, 0);
            tbModel.setValueAt(ruta.getStart(), i, 1);
            tbModel.setValueAt(ruta.getEnd(), i, 2);
            tbModel.setValueAt(ruta.getDistance(), i, 3);
        }

    }

    //Choose CSV File
    private void chooseCSVFile() throws IOException {
        // Create a file chooser
        fileChooser = new JFileChooser();

        // Set a file filter if needed (optional)
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
        fileChooser.setFileFilter(filter);

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(this);

        // Check if a file was chosen
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            JFileSelected = fileChooser.getSelectedFile();

            // Print the path of the selected file
            System.out.println("Selected File: " + JFileSelected.getAbsolutePath());

            // Perform CSV read
            // Perform CSV read and pass the file name
            readCSV();
            checkLists();
            loadRoutes();
        } else {
            System.out.println("No file selected");
        }
    }

// Check if the selected file is a CSV file
    private void readCSV() {
        if (!JFileSelected.getName().toLowerCase().endsWith(".csv")) {
            JOptionPane.showMessageDialog(null, "The selected file is not a CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get the name of the selected file
        String name = JFileSelected.getName();
        int routesCount = 0; // Counter for the number of routes added
        int ignoredRoutesCount = 0; // Counter for ignored routes
        boolean hadDuplicates = false; // Flag indicating if duplicates were found
        boolean hadLessThanThreeElements = false; // Flag indicating if a route had less than three elements

        try (BufferedReader br = new BufferedReader(new FileReader(JFileSelected.getAbsolutePath()))) {
            String line; // Store each line of the CSV file
            boolean firstLine = true; // Flag to skip the header
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip the header
                }

                String[] values = line.split(","); // Split the line into parts using comma as delimiter
                if (values.length >= 3) { // Check if the line has at least three elements
                    String start = values[0]; // Extract start from the CSV
                    String end = values[1]; // Extract end from the CSV
                    int distance = Integer.parseInt(values[2]); // Extract distance from the CSV

                    if (!routeExists(start, end, distance)) { // Check if the route already exists
                        Main.inicioList.add(start); // Add start to inicioList
                        Main.finList.add(end); // Add end to finList
                        Main.routes.add(new Route(Main.generateId("route"), start, end, distance)); // Add a new Route object to routes
                        routesCount++; // Increment the routes counter
                    } else {
                        System.out.println("Route already exists: " + start + " to " + end);
                        ignoredRoutesCount++; // Increment ignored routes counter
                        hadDuplicates = true; // Set flag indicating duplicates were found
                    }
                } else {
                    System.out.println("The line doesn't have enough values: " + line);
                    ignoredRoutesCount++; // Increment ignored routes counter
                    hadLessThanThreeElements = true; // Set flag indicating a route had less than three elements
                }
            }

            // Make our lists bidirectional
            int totalRoutes = Main.inicioList.size(); // Get the total number of routes
            for (int i = 0; i < totalRoutes; i++) {
                Main.inicioList.add(Main.finList.get(i)); // Add end to inicioList
                Main.finList.add(Main.inicioList.get(i)); // Add start to finList
            }

            // Create message for successful loading
            String message = "Loaded " + routesCount + " routes from (" + name + ") successfully.";
            // Append information about ignored routes if any
            if (ignoredRoutesCount > 0) {
                if (hadDuplicates && hadLessThanThreeElements) {
                    message += "\nIgnored " + ignoredRoutesCount + " routes because they were duplicates and had less than 3 elements.";
                } else if (hadDuplicates) {
                    message += "\nIgnored " + ignoredRoutesCount + " routes because they were duplicates.";
                } else if (hadLessThanThreeElements) {
                    message += "\nIgnored " + ignoredRoutesCount + " routes because they had less than 3 elements.";
                }
            }

            // Show a success message dialog
            JOptionPane.showMessageDialog(null, message, "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
            // Refresh the routes table
            refreshRoutesTable();
            // Load the routes into the ComboBoxes after reading the CSV file
            loadRoutes();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File not found: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading the file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid number format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean routeExists(String start, String end, int distance) {
        for (Route route : Main.routes) {
            if (route.getStart().equals(start) && route.getEnd().equals(end) && route.getDistance() == distance) {
                return true;
            }
        }
        return false;
    }

    private void checkLists() {
        // Verifica si Main.inicioList está vacío
        if (Main.inicioList.isEmpty()) {
            System.out.println("Main.inicioList está vacío. No se han agregado elementos.");
        } else {
            System.out.println("==================================");
            System.out.println("Elementos en Main.inicioList:");
            for (String item : Main.inicioList) {
                System.out.println(item);

            }
            System.out.println("==================================");
        }

        // Verifica si Main.finList está vacío
        if (Main.finList.isEmpty()) {
            System.out.println("Main.finList está vacío. No se han agregado elementos.");
        } else {
            System.out.println("Elementos en Main.finList:");
            for (String item : Main.finList) {
                System.out.println(item);
            }
            System.out.println("==================================");
        }
    }

// Load routes into ComboBoxes
    private void loadRoutes() {
        // Store the selected items
        String startSelected = (String) startBox.getSelectedItem();
        String endSelected = (String) endBox.getSelectedItem();

        // Create a model for startBox
        DefaultComboBoxModel<String> startModel = new DefaultComboBoxModel<>();
        // Add selected item back if exists
        if (startSelected != null) {
            startModel.addElement(startSelected);
        }
        // Add elements from inicioList to startModel
        for (int i = 0; i < Main.inicioList.size(); i++) {
            startModel.addElement(Main.inicioList.get(i));
        }
        // Set the startModel to startBox
        startBox.setModel(startModel);

        // Create a model for endBox
        DefaultComboBoxModel<String> endModel = new DefaultComboBoxModel<>();
        // Add selected item back if exists
        if (endSelected != null) {
            endModel.addElement(endSelected);
        }
        // Add elements from finList to endModel
        for (int i = 0; i < Main.finList.size(); i++) {
            endModel.addElement(Main.finList.get(i));
        }
        // Set the endModel to endBox
        endBox.setModel(endModel);
    }

    public DefaultTableModel getTableModel() {
        return (DefaultTableModel) jTable1.getModel();
    }

    private int findDistance(String startLocation, String endLocation) {
        // Buscar la distancia en la tabla de rutas
        for (Route route : Main.routes) {
            if (route.getStart().equals(startLocation) && route.getEnd().equals(endLocation)) {
                return route.getDistance();
            }
        }
        return -1; // Retornar -1 si no se encuentra la ruta
    }

    // Método para verificar si hay pilotos disponibles
    private boolean checkPilotsAvailability() {
        for (boolean available : pilotsAvailable) {
            if (available) {
                return true;
            }
        }
        return false;
    }

    // Método para actualizar el estado de los pilotos disponibles
    private void updatePilotsAvailability() {
        // Suponiendo que desactivamos el primer piloto después de cada viaje
        if (tripsGenerated == 1) {
            pilotsAvailable[0] = false;
        } else if (tripsGenerated == 2) {
            pilotsAvailable[1] = false;
        } else if (tripsGenerated == 3) {
            pilotsAvailable[2] = false;
        }
    }

    

// Método para asignar la imagen correspondiente a cada tipo de vehículo
    private ImageIcon getVehicleIcon(String vehicleType) {
        if (vehicleType.equals("Motorcycle 1")) {
            return new ImageIcon(getClass().getResource("../vehicles/motorcycle_1.gif/"));
        } else if (vehicleType.equals("Motorcycle 2")) {
            return new ImageIcon(getClass().getResource("../vehicles/motorcycle_2.gif"));
        } else if (vehicleType.equals("Motorcycle 3")) {
            return new ImageIcon(getClass().getResource("../vehicles/motorcycle_3.gif"));
        } else if (vehicleType.equals("Standard Vehicle 1")) {
            return new ImageIcon(getClass().getResource("../vehicles/standard_vehicle_1.gif"));
        } else if (vehicleType.equals("Standard Vehicle 2")) {
            return new ImageIcon(getClass().getResource("../vehicles/standard_vehicle_2.gif"));
        } else if (vehicleType.equals("Standard Vehicle 3")) {
            return new ImageIcon(getClass().getResource("../vehicles/standard_vehicle_3.gif"));
        } else if (vehicleType.equals("Premium Vehicle 1")) {
            return new ImageIcon(getClass().getResource("../vehicles/premium_vehicle_1.gif"));
        } else if (vehicleType.equals("Premium Vehicle 2")) {
            return new ImageIcon(getClass().getResource("../vehicles/premium_vehicle_2.gif"));
        } else if (vehicleType.equals("Premium Vehicle 3")) {
            return new ImageIcon(getClass().getResource("../vehicles/premium_vehicle_3.gif"));
        }
        return null; // Si no se encuentra la imagen
    }

// Método para actualizar la disponibilidad de los vehículos
private void updateVehicleAvailability(String vehicleType) {
    // Remover la opción del JComboBox typeBox correspondiente al tipo de vehículo seleccionado
    typeBox.removeItem(vehicleType);
}

    class jPanelGradient extends JPanel {

        private boolean mouseOver = false;

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            Color color1 = new Color(84, 51, 255); // Color base
            Color color2 = new Color(32, 189, 255); // Color al que cambia cuando el mouse está sobre el panel

            if (mouseOver) {
                color1 = new Color(153, 0, 255); // Cambia el color base cuando el mouse está sobre el panel
                color2 = new Color(255, 102, 255); // Cambia el color al que cambia cuando el mouse está sobre el panel
            }

            int width = getWidth();
            int height = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, color1, width, height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }

        public void setMouseOver(boolean mouseOver) {
            this.mouseOver = mouseOver;
            repaint();
        }
    }

    @SuppressWarnings("unchecked")


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new jPanelGradient();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new jPanelGradient();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new jPanelGradient();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new jPanelGradient();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel5 = new jPanelGradient();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel11 = new jPanelGradient();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        btnOpenCSV = new javax.swing.JButton();
        editDistance = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        startBox = new javax.swing.JComboBox<>();
        endBox = new javax.swing.JComboBox<>();
        typeBox = new javax.swing.JComboBox<>();
        noPilotsLabel = new javax.swing.JLabel();
        generateTrip = new javax.swing.JButton();
        panelTripStart = new javax.swing.JPanel();
        transport3 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        start1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        lblTransport1 = new javax.swing.JLabel();
        lblDistance1 = new javax.swing.JLabel();
        lblStart1 = new javax.swing.JLabel();
        lblEnd1 = new javax.swing.JLabel();
        transport1 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        start2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        transport2 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        start3 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        lblBarrera = new javax.swing.JLabel();
        lblTransport2 = new javax.swing.JLabel();
        lblDistance2 = new javax.swing.JLabel();
        lblStart2 = new javax.swing.JLabel();
        lblEnd2 = new javax.swing.JLabel();
        lblTransport3 = new javax.swing.JLabel();
        lblDistance3 = new javax.swing.JLabel();
        lblStart3 = new javax.swing.JLabel();
        lblEnd3 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(102, 255, 204));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setIcon(new javax.swing.ImageIcon("C:\\Users\\Ricardious\\Videos\\logo.png")); // NOI18N
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 80, -1));

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Corbel", 0, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("UDRIVE");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 90, -1, -1));

        jPanel2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel2MouseMoved(evt);
            }
        });
        jPanel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel2MouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel2MouseExited(evt);
            }
        });
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/subir-archivo.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 30, 60));

        jLabel5.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Load routes");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, -1, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 140, 200, 60));

        jPanel3.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel3MouseMoved(evt);
            }
        });
        jPanel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel3MouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel3MouseExited(evt);
            }
        });
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/carro-nuevo.png"))); // NOI18N
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 40, 60));

        jLabel7.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Generate trip");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, -1, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 210, 200, 60));

        jPanel4.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel4MouseMoved(evt);
            }
        });
        jPanel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel4MouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel4MouseExited(evt);
            }
        });
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/ruta.png"))); // NOI18N
        jPanel4.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, 60));

        jLabel9.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Trip start");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 20, -1, -1));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 280, 200, 60));

        jPanel5.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel5MouseMoved(evt);
            }
        });
        jPanel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel5MouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel5MouseExited(evt);
            }
        });
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/historial-de-transacciones.png"))); // NOI18N
        jPanel5.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, 60));

        jLabel11.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Trip history");
        jPanel5.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 20, -1, -1));

        jPanel1.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 350, 200, 60));

        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/cerrar-sesion.png"))); // NOI18N

        jLabel28.setBackground(new java.awt.Color(255, 255, 255));
        jLabel28.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("Logout");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel27)
                .addGap(33, 33, 33)
                .addComponent(jLabel28)
                .addContainerGap(61, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel28)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 420, 200, 60));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 700));

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/login/icons8_Multiply_32px.png"))); // NOI18N
        jLabel13.setToolTipText("");
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel13MouseClicked(evt);
            }
        });
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 10, -1, -1));

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/login/icons8_Expand_Arrow_32px.png"))); // NOI18N
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel12MouseClicked(evt);
            }
        });
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 10, -1, -1));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/Reef.jpg"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 800, 50));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        btnOpenCSV.setText("Load Routes (.csv)");
        btnOpenCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenCSVActionPerformed(evt);
            }
        });

        editDistance.setText("Edit Distance");
        editDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDistanceActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Start", "End", "Distance"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(btnOpenCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(447, 447, 447)
                        .addComponent(editDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 717, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOpenCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("tab1", jPanel6);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        jPanel10.setPreferredSize(new java.awt.Dimension(500, 400));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel24.setBackground(new java.awt.Color(0, 153, 153));
        jLabel24.setFont(new java.awt.Font("Corbel", 2, 24)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(0, 153, 153));
        jLabel24.setText("Select endpoint");
        jPanel10.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 57, -1, -1));

        jLabel25.setBackground(new java.awt.Color(0, 153, 153));
        jLabel25.setFont(new java.awt.Font("Corbel", 2, 24)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(0, 153, 153));
        jLabel25.setText("Select starting point");
        jPanel10.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(49, 57, -1, -1));

        jLabel26.setBackground(new java.awt.Color(0, 153, 153));
        jLabel26.setFont(new java.awt.Font("Corbel", 2, 24)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(0, 153, 153));
        jLabel26.setText("Select type of transportation");
        jPanel10.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(113, 173, -1, -1));

        startBox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        startBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "✻✻Select starting point✻✻" }));
        startBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBoxActionPerformed(evt);
            }
        });
        jPanel10.add(startBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(52, 93, 190, 30));

        endBox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        endBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "✻✻✻Select endpoint✻✻✻" }));
        endBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endBoxActionPerformed(evt);
            }
        });
        jPanel10.add(endBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 93, 193, 30));

        typeBox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        typeBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "✻✻Select type of transportation✻✻", "Motorcycle 1", "Motorcycle 2", "Motorcycle 3", "Standard Vehicle 1", "Standard Vehicle 2", "Standard Vehicle 3", "Premium Vehicle 1", "Premium Vehicle 2", "Premium Vehicle 3" }));
        jPanel10.add(typeBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 215, -1, 30));

        noPilotsLabel.setFont(new java.awt.Font("Corbel", 2, 14)); // NOI18N
        noPilotsLabel.setForeground(new java.awt.Color(255, 0, 0));
        noPilotsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noPilotsLabel.setText("No abiable");
        jPanel10.add(noPilotsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 350, 250, 45));

        generateTrip.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        generateTrip.setText("Generate trip");
        generateTrip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateTripActionPerformed(evt);
            }
        });
        jPanel10.add(generateTrip, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 290, 190, 60));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(150, 150, 150)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(180, 180, 180))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(85, 85, 85)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(188, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab2", jPanel7);

        panelTripStart.setBackground(new java.awt.Color(255, 255, 255));
        panelTripStart.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        panelTripStart.add(transport3, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 450, -1, 50));

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/carretera.png"))); // NOI18N
        panelTripStart.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 480, 570, 30));

        start1.setFont(new java.awt.Font("Corbel", 3, 14)); // NOI18N
        start1.setText("Start");
        start1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                start1MouseClicked(evt);
            }
        });
        start1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start1ActionPerformed(evt);
            }
        });
        panelTripStart.add(start1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 70, -1));

        jButton3.setFont(new java.awt.Font("Corbel", 3, 14)); // NOI18N
        jButton3.setText("Return");
        panelTripStart.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 110, -1, -1));

        lblTransport1.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblTransport1.setText("Pending");
        panelTripStart.add(lblTransport1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, -1, -1));

        lblDistance1.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblDistance1.setText("Pending");
        panelTripStart.add(lblDistance1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        lblStart1.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblStart1.setText("Pending");
        panelTripStart.add(lblStart1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, -1, -1));

        lblEnd1.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblEnd1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEnd1.setText("Pending");
        panelTripStart.add(lblEnd1, new org.netbeans.lib.awtextra.AbsoluteConstraints(608, 60, 160, -1));
        panelTripStart.add(transport1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, -1, 50));
        transport1.getAccessibleContext().setAccessibleDescription("");

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/carretera.png"))); // NOI18N
        panelTripStart.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 110, 580, 30));

        start2.setFont(new java.awt.Font("Corbel", 3, 14)); // NOI18N
        start2.setText("Start");
        start2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start2ActionPerformed(evt);
            }
        });
        panelTripStart.add(start2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 300, 70, -1));

        jButton5.setFont(new java.awt.Font("Corbel", 3, 14)); // NOI18N
        jButton5.setText("Return");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        panelTripStart.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 300, -1, -1));
        panelTripStart.add(transport2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 270, -1, 50));

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/vehicles/carretera.png"))); // NOI18N
        panelTripStart.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 300, 570, 30));

        start3.setFont(new java.awt.Font("Corbel", 3, 14)); // NOI18N
        start3.setText("Start");
        start3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start3ActionPerformed(evt);
            }
        });
        panelTripStart.add(start3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 480, 70, -1));

        jButton7.setFont(new java.awt.Font("Corbel", 3, 14)); // NOI18N
        jButton7.setText("Return");
        panelTripStart.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 480, -1, -1));

        jButton8.setFont(new java.awt.Font("Corbel", 1, 18)); // NOI18N
        jButton8.setText("Start all");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        panelTripStart.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 580, -1, -1));
        panelTripStart.add(lblBarrera, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 0, 10, 660));

        lblTransport2.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblTransport2.setText("Pending");
        panelTripStart.add(lblTransport2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 210, -1, -1));

        lblDistance2.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblDistance2.setText("Pending");
        panelTripStart.add(lblDistance2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 230, -1, -1));

        lblStart2.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblStart2.setText("Pending");
        panelTripStart.add(lblStart2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, -1, -1));

        lblEnd2.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblEnd2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEnd2.setText("Pending");
        panelTripStart.add(lblEnd2, new org.netbeans.lib.awtextra.AbsoluteConstraints(638, 240, 130, -1));

        lblTransport3.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblTransport3.setText("Pending");
        panelTripStart.add(lblTransport3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 390, -1, -1));

        lblDistance3.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblDistance3.setText("Pending");
        panelTripStart.add(lblDistance3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 410, -1, -1));

        lblStart3.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblStart3.setText("Pending");
        panelTripStart.add(lblStart3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 430, -1, -1));

        lblEnd3.setFont(new java.awt.Font("Corbel", 2, 12)); // NOI18N
        lblEnd3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEnd3.setText("Pending");
        panelTripStart.add(lblEnd3, new org.netbeans.lib.awtextra.AbsoluteConstraints(568, 430, 190, -1));

        jTabbedPane1.addTab("tab3", panelTripStart);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Start date/time2", "End date/time", "Distance (km)", "Vehicle", "Fuel consumed"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        jPanel9.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 740, 520));

        jTabbedPane1.addTab("tab4", jPanel9);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, 820, 700));

        setSize(new java.awt.Dimension(1000, 700));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jPanel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseClicked
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jPanel2MouseClicked

    private void jPanel2MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseMoved
        // Verifica si el mouse ya está sobre el panel
        ((jPanelGradient) jPanel2).setMouseOver(true);

    }//GEN-LAST:event_jPanel2MouseMoved

    private void jPanel2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseExited
        ((jPanelGradient) jPanel2).setMouseOver(false);
    }//GEN-LAST:event_jPanel2MouseExited

    private void jPanel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel3MouseClicked
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jPanel3MouseClicked

    private void jPanel3MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel3MouseMoved
        ((jPanelGradient) jPanel3).setMouseOver(true);
    }//GEN-LAST:event_jPanel3MouseMoved

    private void jPanel3MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel3MouseExited
        ((jPanelGradient) jPanel3).setMouseOver(false);
    }//GEN-LAST:event_jPanel3MouseExited

    private void jPanel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseClicked
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jPanel4MouseClicked

    private void jPanel4MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseMoved
        ((jPanelGradient) jPanel4).setMouseOver(true);
    }//GEN-LAST:event_jPanel4MouseMoved

    private void jPanel4MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseExited
        ((jPanelGradient) jPanel4).setMouseOver(false);
    }//GEN-LAST:event_jPanel4MouseExited

    private void jPanel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel5MouseClicked
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_jPanel5MouseClicked

    private void jPanel5MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel5MouseMoved
        ((jPanelGradient) jPanel5).setMouseOver(true);
    }//GEN-LAST:event_jPanel5MouseMoved

    private void jPanel5MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel5MouseExited
        ((jPanelGradient) jPanel5).setMouseOver(false);
    }//GEN-LAST:event_jPanel5MouseExited

    private void jLabel13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MouseClicked
        System.exit(0);
    }//GEN-LAST:event_jLabel13MouseClicked

    private void jLabel12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseClicked
        this.setState(MainFrame.ICONIFIED);
    }//GEN-LAST:event_jLabel12MouseClicked

    private void btnOpenCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenCSVActionPerformed
        // This method is called when the user interacts with the button to open a CSV file.
        try {
            chooseCSVFile(); // Attempt to open a file chooser dialog to select a CSV file.
        } catch (IOException ex) {
            // If an IOException occurs during file selection,
            // log the exception using the Java logging framework.
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnOpenCSVActionPerformed

    private void editDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDistanceActionPerformed
        Distance distance = new Distance(this);
        distance.setVisible(true);

    }//GEN-LAST:event_editDistanceActionPerformed

    private void startBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBoxActionPerformed
        int selectedIndex = startBox.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex <= Main.finList.size()) {
            endBox.setSelectedIndex(selectedIndex);
        }
    }//GEN-LAST:event_startBoxActionPerformed

    private void endBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endBoxActionPerformed
        int selectedIndex = endBox.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex <= Main.inicioList.size()) {
            startBox.setSelectedIndex(selectedIndex);
        }
    }//GEN-LAST:event_endBoxActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

        Journey journey = new Journey(transport1, lblBarrera);
        journey.start();

        Journey journey1 = new Journey(transport2, lblBarrera);
        journey1.start();

        Journey journey2 = new Journey(transport3, lblBarrera);
        journey2.start();


    }//GEN-LAST:event_jButton8ActionPerformed

    private void generateTripActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateTripActionPerformed

        boolean pilotsAvailable = checkPilotsAvailability();
        if (pilotsAvailable) {
            // Get the selected data from the JComboBoxes
            String startLocation = (String) startBox.getSelectedItem();
            String endLocation = (String) endBox.getSelectedItem();
            String transportType = (String) typeBox.getSelectedItem();
            int distance = findDistance(startLocation, endLocation);

            // Create an error message for the options not selected
            String errorMessage = "";
            if (startLocation.equals("✻✻Select starting point✻✻")) {
                errorMessage += "Please select a starting point.\n";
            }
            if (endLocation.equals("✻✻✻Select endpoint✻✻✻")) {
                errorMessage += "Please select an endpoint.\n";
            }
            if (transportType.equals("✻✻Select type of transportation✻✻")) {
                errorMessage += "Please select a type of transportation.\n";
            }

            // Show the error message if necessary
            if (!errorMessage.isEmpty()) {
                JOptionPane.showMessageDialog(this, errorMessage);
                return; // Exit the method if any option is not selected
            }

            // Search for the distance in the routes table
// If the route is found, show success message and update JLabels
            if (tripsGenerated == 0) {
                transport1.setIcon(getVehicleIcon((String) typeBox.getSelectedItem()));
                startLocation1 = (String) startBox.getSelectedItem();
                endLocation1 = (String) endBox.getSelectedItem();
                transportType1 = (String) typeBox.getSelectedItem();
                distance1 = findDistance(startLocation1, endLocation1);

                if (distance1 != -1) {
                    lblStart1.setText(startLocation1);
                    lblEnd1.setText(endLocation1);
                    lblTransport1.setText(transportType1);
                    lblDistance1.setText(String.valueOf("Distance " + distance1 + "Km"));
                    JOptionPane.showMessageDialog(this, "First trip generated successfully!");
                    tripsGenerated++;
                } else {
                    distance1 = findDistance(endLocation1, startLocation1);
                    if (distance1 != -1) {
                        lblStart1.setText(startLocation1);
                        lblEnd1.setText(endLocation1);
                        lblTransport1.setText(transportType1);
                        lblDistance1.setText(String.valueOf("Distance " + distance1 + "Km"));
                        tripsGenerated++;
                        JOptionPane.showMessageDialog(this, "First trip generated successfully!");
                    } else {
                        lblDistance1.setText("Route not found");
                    }
                }
            } else if (tripsGenerated == 1) {
                transport2.setIcon(getVehicleIcon((String) typeBox.getSelectedItem()));
                startLocation2 = (String) startBox.getSelectedItem();
                endLocation2 = (String) endBox.getSelectedItem();
                transportType2 = (String) typeBox.getSelectedItem();
                distance2 = findDistance(startLocation2, endLocation2);

                if (distance2 != -1) {
                    lblStart2.setText(startLocation2);
                    lblEnd2.setText(endLocation2);
                    lblTransport2.setText(transportType2);
                    lblDistance2.setText(String.valueOf(distance2));
                    JOptionPane.showMessageDialog(this, "Second trip generated successfully!");
                    tripsGenerated++;
                } else {
                    distance2 = findDistance(endLocation2, startLocation2);
                    if (distance2 != -1) {
                        lblStart2.setText(startLocation2);
                        lblEnd2.setText(endLocation2);
                        lblTransport2.setText(transportType2);
                        lblDistance2.setText(String.valueOf(distance2));
                        tripsGenerated++;
                        JOptionPane.showMessageDialog(this, "Second trip generated successfully!");
                    } else {
                        lblDistance2.setText("Route not found");
                    }
                }
            } else if (tripsGenerated == 2) {
                transport3.setIcon(getVehicleIcon((String) typeBox.getSelectedItem()));
                startLocation3 = (String) startBox.getSelectedItem();
                endLocation3 = (String) endBox.getSelectedItem();
                transportType3 = (String) typeBox.getSelectedItem();
                distance3 = findDistance(startLocation3, endLocation3);

                if (distance3 != -1) {
                    lblStart3.setText(startLocation3);
                    lblEnd3.setText(endLocation3);
                    lblTransport3.setText(transportType3);
                    lblDistance3.setText(String.valueOf(distance3));
                    JOptionPane.showMessageDialog(this, "Third trip generated successfully!");
                    tripsGenerated++;

                } else {
                    distance3 = findDistance(endLocation3, startLocation3);
                    if (distance3 != -1) {
                        lblStart3.setText(startLocation3);
                        lblEnd3.setText(endLocation3);
                        lblTransport3.setText(transportType3);
                        lblDistance3.setText(String.valueOf(distance3));
                        tripsGenerated++;
                        JOptionPane.showMessageDialog(this, "Third trip generated successfully!");

                    } else {
                        lblDistance3.setText("Route not found");
                    }
                }
                // Después de generar el viaje, actualiza el estado de los pilotos disponibles

            } else {
                JOptionPane.showMessageDialog(this, "You have already generated the maximum number of trips.");

            }
              updatePilotsAvailability(); 
              updateVehicleAvailability(transportType);
        } else {
            // Si no hay pilotos disponibles, deshabilita el botón y muestra el JLabel de pilotos no disponibles
            generateTrip.setEnabled(false);
            noPilotsLabel.setVisible(true); // Muestra el JLabel de pilotos no disponibles
noPilotsLabel.setText("No pilots available at the moment");
            noPilotsLabel.setForeground(Color.RED); // Texto en color rojo
        }
    }//GEN-LAST:event_generateTripActionPerformed

    private void start1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_start1MouseClicked


    }//GEN-LAST:event_start1MouseClicked

    private void start1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start1ActionPerformed

      // Inicia el hilo Journey
    Journey journey = new Journey(transport1, lblBarrera);
    journey.start();
    }//GEN-LAST:event_start1ActionPerformed

    private void start2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start2ActionPerformed

        Journey journey = new Journey(transport2, lblBarrera);
        journey.start();
    }//GEN-LAST:event_start2ActionPerformed

    private void start3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start3ActionPerformed

        Journey journey = new Journey(transport3, lblBarrera);
        journey.start();
    }//GEN-LAST:event_start3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOpenCSV;
    private javax.swing.JButton editDistance;
    private javax.swing.JComboBox<String> endBox;
    private javax.swing.JButton generateTrip;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JTable jTable1;
    private javax.swing.JTable jTable3;
    public javax.swing.JLabel lblBarrera;
    private javax.swing.JLabel lblDistance1;
    private javax.swing.JLabel lblDistance2;
    private javax.swing.JLabel lblDistance3;
    private javax.swing.JLabel lblEnd1;
    private javax.swing.JLabel lblEnd2;
    private javax.swing.JLabel lblEnd3;
    private javax.swing.JLabel lblStart1;
    private javax.swing.JLabel lblStart2;
    private javax.swing.JLabel lblStart3;
    private javax.swing.JLabel lblTransport1;
    private javax.swing.JLabel lblTransport2;
    private javax.swing.JLabel lblTransport3;
    private javax.swing.JLabel noPilotsLabel;
    private javax.swing.JPanel panelTripStart;
    private javax.swing.JButton start1;
    private javax.swing.JButton start2;
    private javax.swing.JButton start3;
    private javax.swing.JComboBox<String> startBox;
    public javax.swing.JLabel transport1;
    public javax.swing.JLabel transport2;
    public javax.swing.JLabel transport3;
    private javax.swing.JComboBox<String> typeBox;
    // End of variables declaration//GEN-END:variables

}
