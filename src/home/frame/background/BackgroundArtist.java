package home.frame.background;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import home.game.GameConstants;

public class BackgroundArtist {

    private List<Star> stars;
    private List<Asteroid> asteroids;
    private java.util.Random random;

    public BackgroundArtist() {
        stars = new ArrayList<>();
        asteroids = new ArrayList<>();
        random = new java.util.Random(4596);

        // Generate individual stars
        for (int i = 0; i < 150; i++) {
            int x = random.nextInt(GameConstants.getGameWidth());
            int y = random.nextInt(GameConstants.getGameHeight());
            int size = random.nextInt(3) + 1;
            int brightness = random.nextInt(156) + 100;

            // Determine star type based on rarity
            Star.StarType type = getRandomStarType();

            stars.add(new Star(x, y, size, brightness, type));
        }

        // Generate star clusters (5-8 clusters)
        int numClusters = 5 + random.nextInt(4);
        for (int c = 0; c < numClusters; c++) {
            generateStarCluster();
        }
    }

    private Star.StarType getRandomStarType() {
        double roll = random.nextDouble();

        // Rarity distribution: Common 60%, Uncommon 25%, Rare 10%, Very Rare 5%
        if (roll < 0.60) {
            return Star.StarType.COMMON;
        } else if (roll < 0.85) {
            return Star.StarType.UNCOMMON;
        } else if (roll < 0.95) {
            return Star.StarType.RARE;
        } else {
            return Star.StarType.VERY_RARE;
        }
    }

    private void generateStarCluster() {
        // Random cluster center
        int clusterX = random.nextInt(GameConstants.getGameWidth());
        int clusterY = random.nextInt(GameConstants.getGameHeight());
        int clusterRadius = 30 + random.nextInt(70); // 30-100 pixel radius
        int numStars = 8 + random.nextInt(15); // 8-22 stars per cluster

        // Determine cluster star type (clusters tend to have similar star types)
        Star.StarType clusterType = getRandomStarType();

        for (int i = 0; i < numStars; i++) {
            // Generate star position within cluster radius using polar coordinates
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * clusterRadius;

            int starX = clusterX + (int) (Math.cos(angle) * distance);
            int starY = clusterY + (int) (Math.sin(angle) * distance);

            // Keep stars within screen bounds
            starX = Math.max(0, Math.min(GameConstants.getGameWidth() - 1, starX));
            starY = Math.max(0, Math.min(GameConstants.getGameHeight() - 1, starY));

            // Cluster stars are typically smaller and slightly dimmer
            int size = 1 + random.nextInt(3); // 1-3 pixels (slightly larger range)
            int brightness = 130 + random.nextInt(90); // Slightly dimmer but not too much

            // 70% chance to use cluster type, 30% chance for random type
            Star.StarType type = (random.nextDouble() < 0.7) ? clusterType : getRandomStarType();

            stars.add(new Star(starX, starY, size, brightness, type));
        }
    }

    private void renderStar(Graphics2D g, Star star) {
        int x = star.getX();
        int y = star.getY();
        int size = star.getSize();

        // Apply transparency to star colors (70% opacity)
        Color starColor = star.getCurrentColor();
        Color transparentColor = new Color(
                starColor.getRed(),
                starColor.getGreen(),
                starColor.getBlue(),
                (int) (255 * 0.7f) // 70% opacity
        );

        // Add glow effect for larger stars (similar to MainMenu)
        if (size > 1) {
            // Draw outer glow first
            Color glowColor = new Color(
                    starColor.getRed(),
                    starColor.getGreen(),
                    starColor.getBlue(),
                    (int) (255 * 0.15f) // Subtle glow
            );
            g.setColor(glowColor);

            switch (star.getShape()) {
                case DIAMOND:
                    drawDiamondShape(g, x + size / 2, y + size / 2, size * 2);
                    break;
                case OVAL:
                    g.fillOval(x - size / 2, y - size / 2, size * 2, size * 2);
                    break;
                case STAR:
                    drawStarShape(g, x + size / 2, y + size / 2, size * 2);
                    break;
            }
        }

        // Draw main star
        g.setColor(transparentColor);
        switch (star.getShape()) {
            case DIAMOND:
                drawDiamondShape(g, x + size / 2, y + size / 2, size);
                break;

            case OVAL:
                g.fillOval(x, y, size, size);
                break;

            case STAR:
                drawStarShape(g, x + size / 2, y + size / 2, size);
                break;
        }
    }

    private void drawDiamondShape(Graphics2D g, int centerX, int centerY, int size) {
        // Draw a diamond (rotated square)
        int halfSize = size / 2;
        int[] xPoints = {
                centerX, // top
                centerX + halfSize, // right
                centerX, // bottom
                centerX - halfSize // left
        };
        int[] yPoints = {
                centerY - halfSize, // top
                centerY, // right
                centerY + halfSize, // bottom
                centerY // left
        };

        g.fillPolygon(xPoints, yPoints, 4);
    }

    private void drawStarShape(Graphics2D g, int centerX, int centerY, int size) {
        // Draw a 5-pointed star
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        double outerRadius = size / 2.0;
        double innerRadius = outerRadius * 0.4;

        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * i / 5.0 - Math.PI / 2; // Start from top
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;

            xPoints[i] = (int) (centerX + radius * Math.cos(angle));
            yPoints[i] = (int) (centerY + radius * Math.sin(angle));
        }

        g.fillPolygon(xPoints, yPoints, 10);
    }

    public void renderBackground(Graphics2D g) {
        // Create gradient background similar to MainMenu
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(5, 5, 20),
                GameConstants.getGameWidth(), GameConstants.getGameHeight(), new Color(20, 20, 50));
        g.setPaint(gradient);
        g.fillRect(0, 0, GameConstants.getGameWidth(), GameConstants.getGameHeight());

        // Render and update stars
        for (Star star : stars) {
            star.update(GameConstants.getGameWidth());

            // Render star based on its shape (color with opacity is handled in renderStar)
            renderStar(g, star);
        }

        // Randomly spawn asteroids (about 1 every 3 seconds at 60fps)
        if (random.nextInt(180) == 0) {
            asteroids.add(new Asteroid());
        }

        // Update and render asteroids
        Iterator<Asteroid> asteroidIterator = asteroids.iterator();
        while (asteroidIterator.hasNext()) {
            Asteroid asteroid = asteroidIterator.next();
            asteroid.update();

            if (asteroid.isActive()) {
                asteroid.render(g);
            } else {
                asteroidIterator.remove(); // Remove inactive asteroids
            }
        }
    }

}
