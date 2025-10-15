package home.game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

public class Explosion {

    private double x;
    private double y;
    private long startTime;
    private long duration; // Duration in milliseconds
    private boolean isActive;
    private ExplosionType type;

    // Different explosion types for different destruction causes
    public enum ExplosionType {
        SHIP_DESTRUCTION(800, 25, new Color[] { Color.ORANGE, Color.RED, Color.YELLOW, Color.DARK_GRAY }),
        COLLISION(600, 20, new Color[] { Color.WHITE, Color.ORANGE, Color.RED, Color.GRAY }),
        PROJECTILE_HIT(400, 15, new Color[] { Color.YELLOW, Color.ORANGE, Color.RED });

        private final long duration;
        private final int maxRadius;
        private final Color[] colors;

        ExplosionType(long duration, int maxRadius, Color[] colors) {
            this.duration = duration;
            this.maxRadius = maxRadius;
            this.colors = colors;
        }

        public long getDuration() {
            return duration;
        }

        public int getMaxRadius() {
            return maxRadius;
        }

        public Color[] getColors() {
            return colors;
        }
    }

    public Explosion(double x, double y, ExplosionType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.startTime = System.currentTimeMillis();
        this.duration = type.getDuration();
        this.isActive = true;
    }

    public void tick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= duration) {
            isActive = false;
        }
    }

    public void render(Graphics2D g) {
        if (!isActive)
            return;

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;
        double progress = (double) elapsed / duration; // 0.0 to 1.0

        if (progress >= 1.0) {
            isActive = false;
            return;
        }

        // Enable high-quality rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Calculate explosion parameters based on progress
        double radius = type.getMaxRadius() * getRadiusProgress(progress);
        double opacity = getOpacityProgress(progress);

        // Create radial gradient for explosion effect
        Color[] colors = type.getColors();
        float[] fractions = createFractions(colors.length);
        Color[] fadedColors = new Color[colors.length];

        // Apply opacity to all colors
        for (int i = 0; i < colors.length; i++) {
            Color original = colors[i];
            fadedColors[i] = new Color(
                    original.getRed(),
                    original.getGreen(),
                    original.getBlue(),
                    (int) (original.getAlpha() * opacity));
        }

        // Draw multiple expanding circles for layered effect
        int numLayers = 3;
        for (int layer = 0; layer < numLayers; layer++) {
            double layerRadius = radius * (0.3 + 0.7 * layer / (numLayers - 1));
            double layerOpacity = opacity * (1.0 - 0.3 * layer / numLayers);

            if (layerRadius > 2) {
                Point2D center = new Point2D.Float((float) x, (float) y);

                // Create gradient with layer-specific opacity
                Color[] layerColors = new Color[fadedColors.length];
                for (int i = 0; i < fadedColors.length; i++) {
                    Color original = fadedColors[i];
                    layerColors[i] = new Color(
                            original.getRed(),
                            original.getGreen(),
                            original.getBlue(),
                            (int) (original.getAlpha() * layerOpacity));
                }

                RadialGradientPaint gradient = new RadialGradientPaint(
                        center, (float) layerRadius, fractions, layerColors);

                g.setPaint(gradient);
                g.fillOval(
                        (int) (x - layerRadius),
                        (int) (y - layerRadius),
                        (int) (layerRadius * 2),
                        (int) (layerRadius * 2));
            }
        }

        // Add spark particles for more dramatic effect
        if (progress < 0.6) {
            drawSparks(g, progress, opacity);
        }
    }

    /**
     * Creates even distribution of gradient fractions
     */
    private float[] createFractions(int colorCount) {
        float[] fractions = new float[colorCount];
        for (int i = 0; i < colorCount; i++) {
            fractions[i] = (float) i / (colorCount - 1);
        }
        return fractions;
    }

    /**
     * Calculate radius expansion over time (starts fast, slows down)
     */
    private double getRadiusProgress(double progress) {
        return Math.sqrt(progress); // Square root for fast expansion that slows down
    }

    /**
     * Calculate opacity fade over time (bright at start, fades out)
     */
    private double getOpacityProgress(double progress) {
        return Math.max(0, 1.0 - Math.pow(progress, 1.5)); // Fade out with slight curve
    }

    /**
     * Draw spark particles radiating from explosion center
     */
    private void drawSparks(Graphics2D g, double progress, double opacity) {
        int sparkCount = 8 + (int) (4 * (1.0 - progress)); // Fewer sparks over time
        double sparkRadius = type.getMaxRadius() * 1.5 * progress;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (opacity * 0.8)));

        for (int i = 0; i < sparkCount; i++) {
            double angle = (2 * Math.PI * i / sparkCount) + (progress * Math.PI); // Rotate over time
            double sparkX = x + Math.cos(angle) * sparkRadius;
            double sparkY = y + Math.sin(angle) * sparkRadius;

            // Random spark size and color
            int sparkSize = 2 + (int) (3 * (1.0 - progress));
            Color sparkColor = new Color(255, 200 + (int) (55 * Math.random()), 0, (int) (255 * opacity));

            g.setColor(sparkColor);
            g.fillOval((int) sparkX - sparkSize / 2, (int) sparkY - sparkSize / 2, sparkSize, sparkSize);
        }

        // Reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean isActive() {
        return isActive;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}