package home.frame.mainmenu.helpmenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class DemoPlanet {
    int x, y, health, maxHealth;
    Color owner;
    String name;
    private final int planetSize = 25;

    DemoPlanet(int x, int y, Color owner, String name, int health) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.name = name;
        this.health = health;
        this.maxHealth = health;
    }

    void draw(Graphics2D g2d) {
        // Draw planet body
        g2d.setColor(owner);
        g2d.fillOval(x - planetSize / 2, y - planetSize / 2, planetSize, planetSize);

        // Draw planet outline
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        g2d.drawOval(x - planetSize / 2, y - planetSize / 2, planetSize, planetSize);

        // Draw health bar
        int barWidth = 30;
        int barHeight = 4;
        float healthPercent = (float) health / maxHealth;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(x - barWidth / 2, y + planetSize / 2 + 5, barWidth, barHeight);

        g2d.setColor(healthPercent > 0.5f ? Color.GREEN : healthPercent > 0.25f ? Color.YELLOW : Color.RED);
        g2d.fillRect(x - barWidth / 2, y + planetSize / 2 + 5, (int) (barWidth * healthPercent), barHeight);

        // Draw name
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(name, x - fm.stringWidth(name) / 2, y - planetSize / 2 - 5);
    }
}
