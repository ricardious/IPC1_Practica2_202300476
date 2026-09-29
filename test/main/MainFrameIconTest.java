package main;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.junit.jupiter.api.Test;

class MainFrameIconTest {
    @Test
    void returnIconFacesTheOppositeDirection() {
        System.setProperty("java.awt.headless", "true");
        BufferedImage source = new BufferedImage(3, 1, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, Color.RED.getRGB());
        source.setRGB(1, 0, Color.GREEN.getRGB());
        source.setRGB(2, 0, Color.BLUE.getRGB());

        Icon mirrored = MainFrame.mirroredIcon(new ImageIcon(source));
        BufferedImage rendered = new BufferedImage(3, 1, BufferedImage.TYPE_INT_ARGB);
        var graphics = rendered.createGraphics();
        try {
            mirrored.paintIcon(null, graphics, 0, 0);
        } finally {
            graphics.dispose();
        }

        assertEquals(3, mirrored.getIconWidth());
        assertEquals(1, mirrored.getIconHeight());
        assertEquals(Color.BLUE.getRGB(), rendered.getRGB(0, 0));
        assertEquals(Color.GREEN.getRGB(), rendered.getRGB(1, 0));
        assertEquals(Color.RED.getRGB(), rendered.getRGB(2, 0));
    }
}
