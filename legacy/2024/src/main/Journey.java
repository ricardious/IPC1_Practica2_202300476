package main;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Ricardious
 */

import javax.swing.*;

public class Journey extends Thread {
    private volatile boolean running = true;
    private JLabel labelToMove;
    private JLabel destinationLabel;
    private int startX;
    private int endX;
    private int stepSize = 5; // Tamaño del paso de movimiento
    private MainFrame mainFrame;

    public Journey(JLabel labelToMove, JLabel destinationLabel) {
        this.labelToMove = labelToMove;
        this.destinationLabel = destinationLabel;

        this.startX = labelToMove.getX();
        this.endX = destinationLabel.getX() - labelToMove.getWidth(); // Ajuste para detenerse justo antes del destino
    }

    public void run() {
        try {
            // Calcula el desplazamiento necesario en el eje X
            int deltaX = endX - startX;
            // Calcula la distancia total que se necesita para llegar justo antes del destino
            double distance = Math.abs(deltaX);
            // Calcula el número total de pasos necesarios para alcanzar el destino
            int numSteps = (int) (distance / stepSize);
            // Calcula el tamaño del paso en el eje X
            double stepX = deltaX / (double) numSteps;

            // Mueve gradualmente el JLabel hacia el destino en el eje X
            for (int i = 0; i < numSteps && running; i++) {
                int newX = (int) Math.round(startX + i * stepX);
                SwingUtilities.invokeLater(() -> {
                    labelToMove.setLocation(newX, labelToMove.getY());
                    this.labelToMove.repaint();

                });
                Thread.sleep(140); // Pausa el hilo para suavizar el movimiento
            }

            SwingUtilities.invokeLater(() -> {
                labelToMove.setLocation(endX, labelToMove.getY());
                this.labelToMove.repaint();

            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
