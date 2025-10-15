package home.game.planets;

import java.awt.Color;

public class Ring {
    public final double innerRadius; // Relative to planet radius
    public final double outerRadius;
    public final Color color;
    public final double opacity;
    public final double rotationOffset; // Additional rotation for variety
    private double tilt = 0; // Currently unused

    public Ring(double innerRadius, double outerRadius, Color color, double opacity, double rotationOffset) {
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.color = color;
        this.opacity = opacity;
        this.rotationOffset = rotationOffset;
    }

    public double getTilt() {
        return tilt;
    }

    public void setTilt(double tilt) {
        this.tilt = tilt;
    }
}
