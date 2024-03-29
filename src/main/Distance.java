package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import static main.Main.routes;
import main.MainFrame;

/**
 *
 * @author Ricardious
 */
public class Distance extends javax.swing.JFrame {
private MainFrame mainFrame;
    private int mouseX, mouseY;
    public Distance(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.setUndecorated(true);
        initComponents();
        this.setLocationRelativeTo(null);

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
    }
    
    
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cancel = new javax.swing.JButton();
        accept = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        enterID = new javax.swing.JTextField();
        enterDistance = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setBackground(new java.awt.Color(0, 204, 204));
        jLabel1.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 153, 153));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Enter the route ID");
        jLabel1.setToolTipText("");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 300, -1));

        jLabel2.setBackground(new java.awt.Color(0, 204, 204));
        jLabel2.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 153, 153));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Enter the new distance");
        jLabel2.setToolTipText("");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 140, 300, -1));

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        getContentPane().add(cancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 230, 80, 30));

        accept.setText("Accept");
        accept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptActionPerformed(evt);
            }
        });
        getContentPane().add(accept, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 230, 80, 30));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/login/icons8_Multiply_32px.png"))); // NOI18N
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 0, -1, 40));

        jLabel3.setIcon(new javax.swing.ImageIcon("C:\\Users\\Ricardious\\Downloads\\Reef.jpg")); // NOI18N
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 300, 40));

        enterID.setFont(new java.awt.Font("Microsoft JhengHei UI", 0, 24)); // NOI18N
        enterID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterIDActionPerformed(evt);
            }
        });
        getContentPane().add(enterID, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 80, 100, 40));

        enterDistance.setFont(new java.awt.Font("Microsoft JhengHei UI", 0, 24)); // NOI18N
        enterDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterDistanceActionPerformed(evt);
            }
        });
        getContentPane().add(enterDistance, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 100, 40));
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 270, 300, 20));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void acceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptActionPerformed
    // Get the ID and distance entered by the user
    int id = Integer.parseInt(enterID.getText());
    int newDistance = Integer.parseInt(enterDistance.getText());

    // Search for the route with the specified ID
    Route routeToUpdate = null;
    for (Route route : routes) {
        if (route.getId() == id) {
            routeToUpdate = route;
            break; // End the loop once the route is found
        }
    }

    // Check if the route with the specified ID was found
    if (routeToUpdate != null) {
        // Update the distance of the found route
        routeToUpdate.setDistance(newDistance);

        // Refresh the routes table
        MainFrame refresFrame = new MainFrame();
        refresFrame.refreshRoutesTable();

        // Find the corresponding row in the table and update it
        DefaultTableModel model = mainFrame.getTableModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((int) model.getValueAt(i, 0) == id) {
                model.setValueAt(newDistance, i, 3); // New values in distance
                break; // End the loop once the row is updated
            }
        }

        // Notify the table that the data has changed
        model.fireTableDataChanged();

        JOptionPane.showMessageDialog(this, "Distance updated successfully.");
        // Close the current window
        this.dispose();
    } else {
        // Show an error message if the route with the specified ID was not found
        JOptionPane.showMessageDialog(this, "Route ID not found.");
    }


    }//GEN-LAST:event_acceptActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancelActionPerformed

    private void enterIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enterIDActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_enterIDActionPerformed

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jLabel4MouseClicked

    private void enterDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enterDistanceActionPerformed

    }//GEN-LAST:event_enterDistanceActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton accept;
    private javax.swing.JButton cancel;
    private javax.swing.JTextField enterDistance;
    private javax.swing.JTextField enterID;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    // End of variables declaration//GEN-END:variables
}
