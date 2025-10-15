package home.game;

import java.util.Random;

public class OrbitParameters {
    // Orbital system constants (use GameConstants)
    private static final double MIN_ORBIT_RADIUS = GameConstants.getMinOrbitRadius(); // Minimum distance from center
    private static final double MAX_ORBIT_RADIUS = Math.min(GameConstants.getGameWidth() / 2,
            GameConstants.getGameHeight() / 2) * GameConstants.getMaxOrbitRadiusFactor()
            - GameConstants.getOrbitRadiusMargin(); // Maximum distance from center (reduced to stay in bounds)
    private static final double MIN_ORBITAL_SPEED = GameConstants.getMinOrbitalSpeed(); // Minimum radians per tick
    private static final double MAX_ORBITAL_SPEED = GameConstants.getMaxOrbitalSpeed(); // Maximum radians per tick
    private static final Random random = new Random();

    private double semiMajorAxis;
    private double semiMinorAxis;
    private double initialAngle;
    private double orbitalSpeed;
    private boolean isVerticalOrbit;
    private double zIndex;

    public OrbitParameters(double semiMajorAxis, double semiMinorAxis, double initialAngle,
            double orbitalSpeed, boolean isVerticalOrbit, double zIndex) {
        this.semiMajorAxis = semiMajorAxis;
        this.semiMinorAxis = semiMinorAxis;
        this.initialAngle = initialAngle;
        this.orbitalSpeed = orbitalSpeed;
        this.isVerticalOrbit = isVerticalOrbit;
        this.zIndex = zIndex;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getSemiMinorAxis() {
        return semiMinorAxis;
    }

    public double getInitialAngle() {
        return initialAngle;
    }

    public double getOrbitalSpeed() {
        return orbitalSpeed;
    }

    public boolean isVerticalOrbit() {
        return isVerticalOrbit;
    }

    public double getZIndex() {
        return zIndex;
    }

    // Constants for orbit generation

    /**
     * Generates orbital parameters with z-index for depth simulation
     */
    public static OrbitParameters generateOrbitParameters() {
        // Random orbital radius - no collision detection needed with z-index system
        double semiMajorAxis = MIN_ORBIT_RADIUS + random.nextDouble() * (MAX_ORBIT_RADIUS - MIN_ORBIT_RADIUS);

        // Determine if this is a circular or elliptical orbit (60% chance circular)
        boolean isCircular = random.nextDouble() < 0.6;

        double semiMinorAxis;
        boolean isVerticalOrbit = false;

        if (isCircular) {
            // Circular orbit
            semiMinorAxis = semiMajorAxis;
        } else {
            // Elliptical orbit
            isVerticalOrbit = random.nextBoolean(); // Random orientation

            // Semi-minor axis is 50-90% of semi-major axis for visible ellipse
            double eccentricity = 0.5 + random.nextDouble() * 0.4; // 0.5 to 0.9
            semiMinorAxis = semiMajorAxis * eccentricity;
        }

        // Random starting angle
        double initialAngle = random.nextDouble() * 2 * Math.PI;

        // Orbital speed - closer planets orbit faster (like real physics)
        // Random direction: 50% chance clockwise (positive), 50% counter-clockwise
        // (negative)
        double baseSpeed = MIN_ORBITAL_SPEED +
                (MAX_ORBITAL_SPEED - MIN_ORBITAL_SPEED)
                        * (1.0 - (semiMajorAxis - MIN_ORBIT_RADIUS) / (MAX_ORBIT_RADIUS - MIN_ORBIT_RADIUS));
        double orbitalSpeed = random.nextBoolean() ? baseSpeed : -baseSpeed;

        // Random z-index for depth (-1 to 1, where positive is closer/larger)
        double zIndex = (semiMajorAxis - MIN_ORBIT_RADIUS) / (MAX_ORBIT_RADIUS - MIN_ORBIT_RADIUS) * 3d - 1d;
        // smaller

        return new OrbitParameters(semiMajorAxis, semiMinorAxis, initialAngle, orbitalSpeed, isVerticalOrbit, zIndex);
    }
}