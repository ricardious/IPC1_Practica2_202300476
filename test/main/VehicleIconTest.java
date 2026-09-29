package main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.ImageIcon;
import org.junit.jupiter.api.Test;

class VehicleIconTest {
    @Test
    void everyVehicleHasAnAnimatedReturnImageWithMatchingCanvas() {
        System.setProperty("java.awt.headless", "true");
        for (String type : new String[]{"motorcycle", "standard_vehicle", "premium_vehicle"}) {
            for (int unit = 1; unit <= 3; unit++) {
                String prefix = "/vehicles/" + type + "_" + unit;
                var outboundUrl = getClass().getResource(prefix + ".gif");
                var returnUrl = getClass().getResource(prefix + "_return.gif");
                assertNotNull(outboundUrl);
                assertNotNull(returnUrl, prefix);
                ImageIcon outbound = new ImageIcon(outboundUrl);
                ImageIcon returning = new ImageIcon(returnUrl);
                assertTrue(returning.getIconWidth() > 0);
                assertEquals(outbound.getIconWidth(), returning.getIconWidth(), prefix);
                assertEquals(outbound.getIconHeight(), returning.getIconHeight(), prefix);
            }
        }
    }
}
