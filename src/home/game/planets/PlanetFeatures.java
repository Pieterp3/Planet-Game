package home.game.planets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stores visual features for planets including craters, rings, and moons.
 * Each planet gets unique features generated based on its position and type.
 */
public class PlanetFeatures {

    private List<Crater> craters;
    private List<Ring> rings;
    private List<Moon> moons;
    private Color surfaceTexture;
    private double rotationSpeed;

    public PlanetFeatures(double planetX, double planetY, PlanetType planetType) {
        // Use planet position as seed for consistent features
        Random rand = new Random((int) (planetX * 1000L + planetY));

        this.craters = new ArrayList<>();
        this.rings = new ArrayList<>();
        this.moons = new ArrayList<>();

        // Set rotation speed based on planet type
        this.rotationSpeed = 0.005 + rand.nextDouble() * 0.015; // 0.005-0.02 radians per update
        if (planetType == PlanetType.SPEED) {
            this.rotationSpeed *= 2; // Speed planets rotate faster
        }

        // Generate surface texture color variation
        int colorVariation = rand.nextInt(40) - 20; // -20 to +20
        this.surfaceTexture = new Color(
                Math.max(0, Math.min(255, 100 + colorVariation)),
                Math.max(0, Math.min(255, 80 + colorVariation)),
                Math.max(0, Math.min(255, 60 + colorVariation)));

        // Generate craters (2-5 craters per planet)
        int craterCount = 2 + rand.nextInt(4);
        for (int i = 0; i < craterCount; i++) {
            double x = (rand.nextDouble() - 0.5) * 1.6; // -0.8 to 0.8
            double y = (rand.nextDouble() - 0.5) * 1.6;
            double size = 0.1 + rand.nextDouble() * 0.2; // 0.1 to 0.3
            double depth = 0.3 + rand.nextDouble() * 0.4; // 0.3 to 0.7
            craters.add(new Crater(x, y, size, depth));
        }

        boolean hasRings = rand.nextDouble() < (planetType == PlanetType.DEFENCE ? 0.45 : 0.15);
        if (hasRings) {
            int ringCount = 1 + rand.nextInt(2); // 1-2 rings
            for (int i = 0; i < ringCount; i++) {
                double inner = 1.3 + i * 0.4 + rand.nextDouble() * 0.2;
                double outer = inner + 0.2 + rand.nextDouble() * 0.3;
                Color ringColor = new Color(
                        150 + rand.nextInt(80),
                        120 + rand.nextInt(60),
                        100 + rand.nextInt(40));
                double opacity = 0.4 + rand.nextDouble() * 0.3; // 0.4 to 0.7
                double rotOffset = rand.nextDouble() * Math.PI * 2;
                rings.add(new Ring(inner, outer, ringColor, opacity, rotOffset));
            }
        }

        // Generate moons (25% chance, more likely for larger planets)
        boolean hasMoons = rand.nextDouble() < 0.25;
        if (hasMoons) {
            int moonCount = 1 + rand.nextInt(4); // 1-4 moons
            for (int i = 0; i < moonCount; i++) {
                double orbitRadius = 2.0 + i * 1.5 + rand.nextDouble() * 1.0;
                double size = 0.15 + rand.nextDouble() * 0.1; // 0.15 to 0.25
                Color moonColor = new Color(
                        180 + rand.nextInt(40),
                        170 + rand.nextInt(40),
                        160 + rand.nextInt(40));
                double orbitSpeed = (0.02 + rand.nextDouble() * 0.03) / (orbitRadius * 0.5); // Slower for distant moons
                double orbitOffset = rand.nextDouble() * Math.PI * 2;
                moons.add(new Moon(orbitRadius, size, moonColor, orbitSpeed, orbitOffset));
            }
        }
    }

    public void updateMoons() {
        for (Moon moon : moons) {
            moon.currentAngle += moon.orbitSpeed;
            if (moon.currentAngle >= 2 * Math.PI) {
                moon.currentAngle -= 2 * Math.PI;
            }
        }
    }

    // Getters
    public List<Crater> getCraters() {
        return craters;
    }

    public List<Ring> getRings() {
        return rings;
    }

    public List<Moon> getMoons() {
        return moons;
    }

    public Color getSurfaceTexture() {
        return surfaceTexture;
    }

    public double getRotationSpeed() {
        return rotationSpeed;
    }
}