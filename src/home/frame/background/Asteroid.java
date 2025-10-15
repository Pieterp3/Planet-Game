package home.frame.background;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

import home.game.GameConstants;

public class Asteroid {

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private int size;
    private Color color;
    private boolean active;

    // Store the asteroid's unique shape
    private int[] shapeXPoints;
    private int[] shapeYPoints;
    private int numShapePoints;

    public Asteroid() {
        // Random spawn location (start from edges)
        Random random = new Random();
        int edge = random.nextInt(4);

        switch (edge) {
            case 0: // Top edge
                x = random.nextInt(GameConstants.getGameWidth());
                y = -10;
                break;
            case 1: // Right edge
                x = GameConstants.getGameWidth() + 10;
                y = random.nextInt(GameConstants.getGameHeight());
                break;
            case 2: // Bottom edge
                x = random.nextInt(GameConstants.getGameWidth());
                y = GameConstants.getGameHeight() + 10;
                break;
            case 3: // Left edge
                x = -10;
                y = random.nextInt(GameConstants.getGameHeight());
                break;
        }

        // Random target point for trajectory
        double targetX = random.nextDouble() * GameConstants.getGameWidth();
        double targetY = random.nextDouble() * GameConstants.getGameHeight();

        // Calculate velocity to reach target
        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Normalize and set speed (2-5 pixels per frame)
        double speed = 2 + random.nextDouble() * 3;
        velocityX = (deltaX / distance) * speed;
        velocityY = (deltaY / distance) * speed;

        // Random size (1-3 pixels)
        size = 2 + random.nextInt(7);

        // Grayish color with slight variation
        int grayValue = 150 + random.nextInt(80);
        color = new Color(grayValue, grayValue - 10, grayValue - 20);

        // Generate unique asteroid shape during creation
        generateAsteroidShape(random);

        active = true;
    }

    /**
     * Generates a unique wavy shape for this asteroid that will remain consistent
     */
    private void generateAsteroidShape(Random random) {
        numShapePoints = Math.max(8, size); // More points for larger asteroids
        shapeXPoints = new int[numShapePoints];
        shapeYPoints = new int[numShapePoints];

        for (int p = 0; p < numShapePoints; p++) {
            double angle = (p * 2 * Math.PI) / numShapePoints;

            // Create irregular rocky distortion with multiple frequencies
            double wavePhase1 = angle * 3 + random.nextDouble() * Math.PI * 2; // Primary irregular pattern
            double wavePhase2 = angle * 7 + random.nextDouble() * Math.PI * 2; // Secondary detail
            double wavePhase3 = angle * 11 + random.nextDouble() * Math.PI * 2; // Fine rocky texture

            // Combine multiple wave frequencies for complex rocky edges
            double waveOffset = 0.2 * Math.sin(wavePhase1) +
                    0.1 * Math.sin(wavePhase2) +
                    0.05 * Math.sin(wavePhase3);

            // Apply wave distortion to create irregular radius (relative to center)
            double baseRadius = size / 2.0;
            double wavyRadius = baseRadius * (0.7 + 0.3 * (1.0 + waveOffset));

            // Store relative coordinates (will be offset during rendering)
            shapeXPoints[p] = (int) (Math.cos(angle) * wavyRadius);
            shapeYPoints[p] = (int) (Math.sin(angle) * wavyRadius);
        }
    }

    public void update() {
        if (!active)
            return;

        x += velocityX;
        y += velocityY;

        // Deactivate if off screen
        if (x < -20 || x > GameConstants.getGameWidth() + 20 ||
                y < -20 || y > GameConstants.getGameHeight() + 20) {
            active = false;
        }
    }

    public void render(Graphics2D g) {
        if (!active)
            return;

        // Enable anti-aliasing for smooth edges
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw main asteroid with wavy, irregular edges
        drawWavyAsteroid(g, (int) x, (int) y, size, color);

        // Draw fading trail with wavy asteroids
        for (int i = 1; i <= 5; i++) {
            double trailX = x - velocityX * i * 0.5;
            double trailY = y - velocityY * i * 0.5;

            // Check if trail point is on screen
            if (trailX >= 0 && trailX < GameConstants.getGameWidth() &&
                    trailY >= 0 && trailY < GameConstants.getGameHeight()) {

                float alpha = Math.max(0, 0.8f - (i * 0.15f));
                Color trailColor = new Color(
                        color.getRed() / 255.0f,
                        color.getGreen() / 255.0f,
                        color.getBlue() / 255.0f,
                        alpha);

                int trailSize = Math.max(1, size - i);
                drawWavyAsteroid(g, (int) trailX, (int) trailY, trailSize, trailColor);
            }
        }

        // Reset anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Draws an asteroid using its pre-generated wavy shape
     */
    private void drawWavyAsteroid(Graphics2D g, int centerX, int centerY, int asteroidSize, Color asteroidColor) {
        // Use the stored shape, scaling it if needed for trail effects
        double sizeScale = asteroidSize / (double) size; // Scale factor for trail rendering

        int[] xPoints = new int[numShapePoints];
        int[] yPoints = new int[numShapePoints];

        // Apply the stored shape with current position and scale
        for (int p = 0; p < numShapePoints; p++) {
            xPoints[p] = (int) (centerX + shapeXPoints[p] * sizeScale);
            yPoints[p] = (int) (centerY + shapeYPoints[p] * sizeScale);
        }

        // Draw the wavy asteroid shape
        g.setColor(asteroidColor);
        g.fillPolygon(xPoints, yPoints, numShapePoints);

        // Add subtle darker outline for definition
        g.setColor(asteroidColor.darker());
        g.drawPolygon(xPoints, yPoints, numShapePoints);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}