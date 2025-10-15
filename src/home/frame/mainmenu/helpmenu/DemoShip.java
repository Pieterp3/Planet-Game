package home.frame.mainmenu.helpmenu;

import java.awt.Color;
import java.awt.Graphics2D;

public class DemoShip {
    float x, y, targetX, targetY, progress;
    Color owner;
    boolean arrived;

    DemoShip(int startX, int startY, int targetX, int targetY, Color owner) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.owner = owner;
        this.progress = 0.0f;
        this.arrived = false;
    }

    void update() {
        if (!arrived) {
            progress += 0.02f; // Faster movement for better visibility
            if (progress >= 1.0f) {
                progress = 1.0f;
                arrived = true;
            }

            // Interpolate position
            x = x + (targetX - x) * 0.03f;
            y = y + (targetY - y) * 0.03f;
        }
    }

    boolean hasReachedTarget() {
        return arrived && progress >= 1.0f;
    }

    void draw(Graphics2D g2d) {
        if (!arrived) {
            // Make ships larger and more visible
            g2d.setColor(owner);
            g2d.fillOval((int) x - 5, (int) y - 5, 10, 10);

            // White outline for visibility
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new java.awt.BasicStroke(1.0f));
            g2d.drawOval((int) x - 5, (int) y - 5, 10, 10);

            // Draw trail
            g2d.setColor(new Color(owner.getRed(), owner.getGreen(), owner.getBlue(), 150));
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            g2d.drawLine((int) x, (int) y, (int) (x - (targetX - x) * 0.2f), (int) (y - (targetY - y) * 0.2f));
        }
    }
}
