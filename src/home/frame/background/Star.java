package home.frame.background;

import java.awt.Color;
import home.game.GameConstants;

public class Star {

    public enum StarType {
        COMMON, UNCOMMON, RARE, VERY_RARE
    }

    public enum StarShape {
        DIAMOND, OVAL, STAR
    }

    private double x;
    private double y;
    private double baseY;
    private int baseSize;
    private int currentSize;
    private int brightness;

    private double twinkleFactor;
    private double twinkleRate;
    private double movementSpeed;
    private double sineOffset;
    private double sineAmplitude;
    private double secondarySineOffset;
    private double secondaryAmplitude;
    private double verticalDrift;
    private StarType type;
    private StarShape starShape;
    private Color baseColor;

    public Star(int x, int y, int size, int brightness, StarType type) {
        this.x = x;
        this.y = y;
        this.baseY = y;
        this.baseSize = size;
        this.currentSize = size;
        this.brightness = brightness;
        this.type = type;

        // Smaller stars are further away, so they twinkle slower
        this.twinkleRate = 0.1 / size; // Larger size = faster twinkle
        this.twinkleFactor = Math.random() * Math.PI * 2; // Random starting phase

        // Movement properties - slower for smaller (distant) stars
        this.movementSpeed = 0.1 + (size * 0.05); // 0.1 to 0.25 pixels per frame
        this.sineOffset = Math.random() * Math.PI * 2; // Random sine wave offset
        this.sineAmplitude = 10 + (size * 5); // 10-25 pixel amplitude based on size

        // Secondary sine wave for more complex movement
        this.secondarySineOffset = Math.random() * Math.PI * 2;
        this.secondaryAmplitude = 5 + (size * 2); // Smaller secondary amplitude

        // Slow vertical drift
        this.verticalDrift = (Math.random() - 0.5) * 0.02; // -0.01 to +0.01 pixels per frame

        // Random star shape
        java.util.Random random = new java.util.Random();
        StarShape[] shapes = StarShape.values();
        this.starShape = shapes[random.nextInt(shapes.length)];

        // Set base color based on star type
        switch (type) {
            case COMMON:
                this.baseColor = Color.YELLOW;
                break;
            case UNCOMMON:
                this.baseColor = Color.RED;
                break;
            case RARE:
                this.baseColor = Color.BLUE;
                break;
            case VERY_RARE:
                this.baseColor = Color.WHITE;
                break;
        }
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public int getSize() {
        return currentSize;
    }

    public int getBrightness() {
        return brightness;
    }

    public Color getCurrentColor() {
        // Calculate color shift based on twinkle
        double colorShift = Math.sin(twinkleFactor) * 0.3; // 30% max shift

        int r = Math.max(0, Math.min(255, (int) (baseColor.getRed() * (1 + colorShift * 0.5))));
        int g = Math.max(0, Math.min(255, (int) (baseColor.getGreen() * (1 + colorShift * 0.5))));
        int b = Math.max(0, Math.min(255, (int) (baseColor.getBlue() * (1 + colorShift * 0.5))));

        // Apply brightness
        float brightnessFactor = brightness / 255.0f;
        r = (int) (r * brightnessFactor);
        g = (int) (g * brightnessFactor);
        b = (int) (b * brightnessFactor);

        return new Color(r, g, b);
    }

    public StarType getType() {
        return type;
    }

    public StarShape getShape() {
        return starShape;
    }

    public void update(int screenWidth) {
        // Move horizontally
        x += movementSpeed;

        // Wrap around screen
        if (x > screenWidth + baseSize) {
            x = -baseSize;
        }

        // Complex sine wave vertical movement with secondary wave and drift
        double primaryWave = Math.sin((x * 0.01) + sineOffset) * sineAmplitude;
        double secondaryWave = Math.sin((x * 0.03) + secondarySineOffset) * secondaryAmplitude;

        // Add vertical drift over time
        baseY += verticalDrift;

        // Wrap baseY if it goes too far off screen
        if (baseY < -50)
            baseY = GameConstants.getGameHeight() + 50;
        if (baseY > GameConstants.getGameHeight() + 50)
            baseY = -50;

        y = baseY + primaryWave + secondaryWave;

        // Vary brightness with sine wave
        double brightnessVariation = Math.sin(twinkleFactor) * 30;
        brightness = (int) (200 + brightnessVariation); // Base brightness 200 +/- 30
        brightness = Math.max(100, Math.min(255, brightness));

        // Vary size by a smaller amount to ensure stars stay visible
        // For size 1 stars: vary by ±0.3, for larger stars: vary by more
        double maxVariation = Math.min(0.8, baseSize * 0.3);
        double sizeVariation = Math.sin(twinkleFactor * 1.5) * maxVariation;
        currentSize = (int) Math.round(baseSize + sizeVariation);
        currentSize = Math.max(1, currentSize); // Ensure minimum size of 1 pixel

        // Advance twinkle animation at the star's specific rate
        twinkleFactor += twinkleRate;
    }

    // Keep the old twinkle method for backward compatibility
    public void twinkle() {
        update(GameConstants.getGameWidth()); // Default screen width
    }

}
