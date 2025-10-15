package home.game.planets;

import java.awt.Color;

public class Moon {
    public final double orbitRadius; // Distance from planet center
    public final double size; // Relative to planet size
    public final Color color;
    public final double orbitSpeed; // Radians per update
    public final double orbitOffset; // Initial angle offset
    public double currentAngle; // Current orbital position

    public Moon(double orbitRadius, double size, Color color, double orbitSpeed, double orbitOffset) {
        this.orbitRadius = orbitRadius;
        this.size = size;
        this.color = color;
        this.orbitSpeed = orbitSpeed;
        this.orbitOffset = orbitOffset;
        this.currentAngle = orbitOffset;
    }
}