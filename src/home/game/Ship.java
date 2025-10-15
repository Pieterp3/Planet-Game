package home.game;

import java.util.List;

import home.game.operators.Bot;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.planets.Planet;

public class Ship {

    private Operator operator;
    private Game game; // Reference to game for ability checks
    private Planet origin;
    private Planet destination;
    private double speed;
    private int health;
    private int damage;
    private double x;
    private double y;
    private double direction;// angle in radians

    // Movement control
    private boolean stationary;

    // Persistent combat states
    private boolean isFleeing = false;
    private boolean isChasing = false;
    private Ship chaseTarget = null;

    // Special ship flags
    private boolean isMissile = false; // Missiles don't participate in combat or collision

    // Lifetime tracking
    private long creationTime;
    private long maxLifetime; // Maximum lifetime in milliseconds

    public Ship(Operator operator, Planet origin, Planet destination, double speed, int health, int damage) {
        this.operator = operator;
        this.origin = origin;
        if (origin != null) {
            this.x = origin.getX();
            this.y = origin.getY();
        } else {
            this.x = GameConstants.getGameWidth() / 2;
            this.y = GameConstants.getGameHeight() / 2;
        }
        this.destination = destination;
        this.speed = speed;
        this.health = health;
        this.damage = damage;

        // Initialize movement control
        this.stationary = false;
        this.isMissile = false;

        // Initialize lifetime - random between 10-15 seconds
        this.creationTime = System.currentTimeMillis();
        this.maxLifetime = 10000 + (long) (Math.random() * 5000); // 10-15 seconds
    }

    /**
     * Constructor for special missile ships that don't participate in
     * combat/collision
     */
    public Ship(Operator operator, Planet origin, Planet destination, double speed, int health, int damage,
            boolean isMissile) {
        this.operator = operator;
        this.origin = origin;
        if (origin != null) {
            this.x = origin.getX();
            this.y = origin.getY();
        } else {
            this.x = GameConstants.getGameWidth() / 2;
            this.y = GameConstants.getGameHeight() / 2;
        }
        this.destination = destination;
        this.speed = speed;
        this.health = health;
        this.damage = damage;

        // Initialize movement control
        this.stationary = false;
        this.isMissile = isMissile;

        // Initialize lifetime - missiles have shorter lifetime (5-8 seconds), regular
        // ships 10-15 seconds
        this.creationTime = System.currentTimeMillis();
        if (isMissile) {
            this.maxLifetime = 5000 + (long) (Math.random() * 3000); // 5-8 seconds for missiles
        } else {
            this.maxLifetime = 10000 + (long) (Math.random() * 5000); // 10-15 seconds for regular ships
        }
    }

    public void resetCreationTime() {
        this.creationTime = System.currentTimeMillis();
    }

    public void tick() {
        // Check if ship has exceeded its maximum lifetime
        if (System.currentTimeMillis() - creationTime > maxLifetime) {
            // Ship expired - create small explosion and remove
            createExplosion(Explosion.ExplosionType.SHIP_DESTRUCTION);
            operator.removeShip(this);
            return;
        }

        // Missiles only move towards target - no combat, collision, or avoidance
        if (isMissile) {
            // Simple direct movement to destination
            double dx = destination.getX() - x;
            double dy = destination.getY() - y;
            double distance = Math.hypot(dx, dy);

            if (distance < speed) {
                // Reached destination - inflict damage and remove
                destination.takeDamage(this);
                operator.removeShip(this);
            } else {
                // Store old position for debugging
                double oldX = x;
                double oldY = y;

                // Move directly toward destination
                direction = Math.atan2(dy, dx);
                x += Math.cos(direction) * speed;
                y += Math.sin(direction) * speed;

                // Debug: Check for invalid missile movement
                if ((x < 5 && y < 5) || Double.isNaN(x) || Double.isNaN(y)) {
                    // Fix invalid positions
                    if (Double.isNaN(x) || x < 10)
                        x = Math.max(10, oldX);
                    if (Double.isNaN(y) || y < 10)
                        y = Math.max(10, oldY);
                }
            }
            return; // Skip all other logic for missiles
        }

        // Combat is now handled by CombatManager, we only handle movement here
        boolean isCurrentlyStationary = isStationary();
        boolean inCombat = isInCombat();

        // Always check for emergency avoidance (even during combat)
        if (needsEmergencyAvoidance()) {
            performEmergencyAvoidance();
        } else if (inCombat && isCurrentlyStationary) {
            // Perform combat maneuvers while fighting
            performCombatManeuvers();
        } else if (!isCurrentlyStationary) {
            move();
            // Check if reached destination
            if (Math.hypot(destination.getX() - x, destination.getY() - y) < speed) {
                // Inflict damage to the planet
                destination.takeDamage(this);
                // Remove ship from game
                operator.removeShip(this);
            }
        }

        // Check for ship-to-ship collisions after movement
        checkShipCollisions();

        // Check for sun collision
        checkSunCollision();
    }

    public void setLocation(double x, double y) {
        if (x < 5 && y < 5) {
            Thread.dumpStack();
        }
        double safeX = Math.max(10, Math.min(GameConstants.getGameWidth() - 10, x));
        double safeY = Math.max(10, Math.min(GameConstants.getGameHeight() - 10, y));
        this.x = safeX;
        this.y = safeY;
    }

    public void move() {
        // Check for interception targets first
        Ship interceptTarget = findInterceptionTarget();

        double targetX, targetY;
        if (interceptTarget != null) {
            // Target the intercepting ship
            targetX = interceptTarget.getX();
            targetY = interceptTarget.getY();
        } else {
            // Target the predicted position of the destination planet
            double[] predictedPos = predictPlanetPosition(destination);
            targetX = predictedPos[0];
            targetY = predictedPos[1];
        }

        // Calculate desired direction towards target
        double desiredAngle = Math.atan2(targetY - y, targetX - x);

        // Apply smart pathfinding with obstacle avoidance
        double adjustedAngle = calculateSmartPath(desiredAngle, targetX, targetY);

        // Additional safety check for NaN angle
        if (Double.isNaN(adjustedAngle)) {
            adjustedAngle = desiredAngle;
        }

        // Store old position for debugging
        double oldX = x;
        double oldY = y;

        // Calculate movement deltas
        double deltaX = speed * Math.cos(adjustedAngle);
        double deltaY = speed * Math.sin(adjustedAngle);

        // Check for NaN movement deltas
        if (Double.isNaN(deltaX) || Double.isNaN(deltaY)) {
            return; // Skip movement this tick
        }

        // Move in the adjusted direction
        x += deltaX;
        y += deltaY;
        direction = adjustedAngle;

        // Debug: Check if movement resulted in (0,0) or NaN
        if ((x < 5 && y < 5) || Double.isNaN(x) || Double.isNaN(y)) {
            // Fix invalid positions
            if (Double.isNaN(x) || x < 10)
                x = Math.max(10, oldX);
            if (Double.isNaN(y) || y < 10)
                y = Math.max(10, oldY);
        }
    }

    /**
     * Finds enemy ships to intercept if the destination planet targets our origin
     * 
     * @return Ship to intercept, or null if no interception needed
     */
    private Ship findInterceptionTarget() {
        // Check if destination planet has origin as a target
        if (!destination.getTargets().contains(origin)) {
            return null; // No mutual targeting, proceed normally
        }

        // Find ships from destination planet that are targeting our origin
        // We need access to the game's ship list, so we'll need to get it through the
        // operator
        List<Ship> allShips = operator.getGame().getShips();

        Ship closestThreat = null;
        double closestDistance = Double.MAX_VALUE;

        for (Ship enemyShip : allShips) {
            // Check if this is an enemy ship from our destination targeting our origin
            if (enemyShip.getOperator() != this.operator &&
                    enemyShip.getOrigin() == this.destination &&
                    enemyShip.getDestination() == this.origin) {

                // Calculate distance to this enemy ship
                double distance = Math.hypot(enemyShip.getX() - x, enemyShip.getY() - y);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestThreat = enemyShip;
                }
            }
        }

        return closestThreat;
    }

    /**
     * Predicts where a planet will be in the future to improve targeting
     * 
     * @param planet The planet to predict position for
     * @return Array with [x, y] coordinates of predicted position
     */
    private double[] predictPlanetPosition(Planet planet) {
        // If planet is static, return current position
        if (planet.getOrbitalSpeed() == 0) {
            return new double[] { planet.getX(), planet.getY() };
        }

        // Calculate current distance to planet
        double currentDistance = Math.hypot(planet.getX() - x, planet.getY() - y);

        // Use iterative approach to find optimal interception point
        double bestTime = 0;
        double bestDistance = Double.MAX_VALUE;

        // Test different time intervals to find the best interception point
        for (double testTime = 0; testTime <= currentDistance / speed * 2; testTime += 0.5) {
            // Calculate where planet will be at this time
            double futureAngle = planet.getOrbitalAngle() + (planet.getOrbitalSpeed() * testTime);

            double predictedX, predictedY;
            if (planet.isVerticalOrbit()) {
                predictedX = planet.getOrbitCenterX() + planet.getSemiMinorAxis() * Math.cos(futureAngle);
                predictedY = planet.getOrbitCenterY() + planet.getSemiMajorAxis() * Math.sin(futureAngle);
            } else {
                predictedX = planet.getOrbitCenterX() + planet.getSemiMajorAxis() * Math.cos(futureAngle);
                predictedY = planet.getOrbitCenterY() + planet.getSemiMinorAxis() * Math.sin(futureAngle);
            }

            // Calculate how far we can travel in this time
            double distanceWeCanTravel = speed * testTime;

            // Calculate distance from our current position to predicted planet position
            double distanceToPredictedPosition = Math.hypot(predictedX - x, predictedY - y);

            // Find the time where our travel distance matches the distance to the predicted
            // position
            double timeDifference = Math.abs(distanceWeCanTravel - distanceToPredictedPosition);
            if (timeDifference < bestDistance) {
                bestDistance = timeDifference;
                bestTime = testTime;
            }
        }

        // Calculate the final interception position
        double finalAngle = planet.getOrbitalAngle() + (planet.getOrbitalSpeed() * bestTime);

        double interceptX, interceptY;
        if (planet.isVerticalOrbit()) {
            interceptX = planet.getOrbitCenterX() + planet.getSemiMinorAxis() * Math.cos(finalAngle);
            interceptY = planet.getOrbitCenterY() + planet.getSemiMajorAxis() * Math.sin(finalAngle);
        } else {
            interceptX = planet.getOrbitCenterX() + planet.getSemiMajorAxis() * Math.cos(finalAngle);
            interceptY = planet.getOrbitCenterY() + planet.getSemiMinorAxis() * Math.sin(finalAngle);
        }

        return new double[] { interceptX, interceptY };
    }

    /**
     * Checks for collisions with other ships and handles collision damage
     * Only checks collisions with enemy ships - friendly ships don't collide
     */
    private void checkShipCollisions() {
        final double COLLISION_DISTANCE = GameConstants.getShipSize() + 2; // Ship collision radius

        List<Ship> allShips = operator.getGame().getShips();
        for (Ship otherShip : allShips) {
            if (otherShip == this || otherShip.isDestroyed() || this.isDestroyed()) {
                continue; // Don't check collision with self or destroyed ships
            }

            // Skip collision check with friendly ships (same operator)
            if (otherShip.getOperator() == this.operator) {
                continue; // Friendly ships don't collide with each other
            }

            // Calculate distance between ships
            double distance = Math.hypot(otherShip.getX() - x, otherShip.getY() - y);

            // Check if collision occurred
            if (distance < COLLISION_DISTANCE) {
                handleShipCollision(otherShip);
                return; // Exit after handling one collision to prevent multiple collisions in same tick
            }
        }
    }

    /**
     * Handles collision between this ship and another ship based on health
     */
    private void handleShipCollision(Ship otherShip) {
        // Prevent processing the same collision twice
        if (this.isDestroyed() || otherShip.isDestroyed()) {
            return;
        }

        int myHealth = this.health;
        int otherHealth = otherShip.health;

        if (myHealth == otherHealth) {
            // Equal health - both ships destroyed
            this.health = 0;
            otherShip.health = 0;

            // Create explosions for both ships
            createExplosion(Explosion.ExplosionType.COLLISION);
            otherShip.createExplosion(Explosion.ExplosionType.COLLISION);

            // Remove both ships from the game
            this.operator.removeShip(this);
            otherShip.operator.removeShip(otherShip);

        } else if (myHealth > otherHealth) {
            // This ship has more health - it survives, other ship dies
            this.health -= otherHealth; // Lose health equal to other ship's remaining health
            otherShip.health = 0;

            // Create explosion for destroyed ship
            otherShip.createExplosion(Explosion.ExplosionType.COLLISION);

            // Remove the destroyed ship
            otherShip.operator.removeShip(otherShip);
        } else {
            // Other ship has more health - this ship dies, other survives
            otherShip.health -= myHealth; // Other ship loses health equal to this ship's remaining health
            this.health = 0;

            // Create explosion for this destroyed ship
            this.createExplosion(Explosion.ExplosionType.COLLISION);

            // Remove this ship
            this.operator.removeShip(this);
        }
    }

    /**
     * Creates an explosion at this ship's location
     */
    private void createExplosion(Explosion.ExplosionType type) {
        if (operator != null && operator.getGame() != null) {
            Explosion explosion = new Explosion(x, y, type);
            operator.getGame().addExplosion(explosion);
        }
    }

    /**
     * Checks for collision with the central sun and destroys the ship if it gets
     * too close
     */
    private void checkSunCollision() {
        double centralStarX = GameConstants.getGameWidth() / 2.0;
        double centralStarY = GameConstants.getGameHeight() / 2.0;
        double sunDistance = Math.hypot(centralStarX - x, centralStarY - y);

        // Sun collision radius - smaller than emergency avoidance distance
        final double SUN_COLLISION_RADIUS = 20;

        if (sunDistance <= SUN_COLLISION_RADIUS) {
            // Ship collided with sun - create explosion and destroy ship
            createExplosion(Explosion.ExplosionType.SHIP_DESTRUCTION);

            // Set health to 0 and remove from game
            this.health = 0;
            operator.removeShip(this);
        }
    }

    /**
     * Checks if the ship needs emergency avoidance (about to collide with sun or
     * planet)
     */
    private boolean needsEmergencyAvoidance() {
        final double EMERGENCY_DISTANCE = 25; // Very close collision threshold

        // Check sun collision
        double centralStarX = GameConstants.getGameWidth() / 2.0;
        double centralStarY = GameConstants.getGameHeight() / 2.0;
        double sunDistance = Math.hypot(centralStarX - x, centralStarY - y);

        if (sunDistance < EMERGENCY_DISTANCE) {
            return true;
        }

        // Check planet collisions (including moving planets)
        List<Planet> allPlanets = operator.getGame().getPlanets();
        for (Planet planet : allPlanets) {
            if (planet == destination) {
                continue; // Don't emergency avoid our destination
            }

            double planetDistance = Math.hypot(planet.getX() - x, planet.getY() - y);
            if (planetDistance < EMERGENCY_DISTANCE) {
                return true;
            }
        }

        return false;
    }

    /**
     * Performs emergency avoidance movement to prevent collision
     */
    private void performEmergencyAvoidance() {
        double avoidanceX = 0;
        double avoidanceY = 0;

        // Emergency sun avoidance
        double centralStarX = GameConstants.getGameWidth() / 2.0;
        double centralStarY = GameConstants.getGameHeight() / 2.0;
        double sunDistance = Math.hypot(centralStarX - x, centralStarY - y);

        if (sunDistance > 0 && sunDistance < 30) {
            avoidanceX += (x - centralStarX) / sunDistance;
            avoidanceY += (y - centralStarY) / sunDistance;
        }

        // Emergency planet avoidance
        List<Planet> allPlanets = operator.getGame().getPlanets();
        for (Planet planet : allPlanets) {
            if (planet == destination) {
                continue;
            }

            double planetDistance = Math.hypot(planet.getX() - x, planet.getY() - y);
            if (planetDistance > 0 && planetDistance < 30) {
                avoidanceX += (x - planet.getX()) / planetDistance;
                avoidanceY += (y - planet.getY()) / planetDistance;
            }
        }

        // Normalize avoidance direction
        double avoidanceLength = Math.hypot(avoidanceX, avoidanceY);
        if (avoidanceLength > 0) {
            avoidanceX /= avoidanceLength;
            avoidanceY /= avoidanceLength;

            // Store old position for debugging
            double oldX = x;
            double oldY = y;

            // Move at full speed away from danger
            x += speed * avoidanceX;
            y += speed * avoidanceY;
            direction = Math.atan2(avoidanceY, avoidanceX);

            // Debug: Check for invalid movement
            if ((x < 5 && y < 5) || Double.isNaN(x) || Double.isNaN(y)) {
                // Fix invalid positions
                if (Double.isNaN(x) || x < 10)
                    x = Math.max(10, oldX);
                if (Double.isNaN(y) || y < 10)
                    y = Math.max(10, oldY);
            }
        }
    }

    /**
     * Performs advanced combat maneuvers with persistent states to prevent
     * oscillation
     * Ships commit to flee/chase decisions until combat is resolved
     */
    private void performCombatManeuvers() {
        Ship currentTarget = getCombatTarget();

        // Handle persistent fleeing state
        if (isFleeing) {
            performFleeManeuver(currentTarget);

            // Check if we can exit flee state
            if (canExitFleeState()) {
                isFleeing = false;
                // Continue to destination after successful escape
            }
            return;
        }

        // Handle persistent chasing state
        if (isChasing && chaseTarget != null) {
            performChaseManeuver(chaseTarget);

            // Check if we should exit chase state
            if (canExitChaseState()) {
                isChasing = false;
                chaseTarget = null;
                // Continue to destination after chase ends
            }
            return;
        }

        // Initial combat state decision (only made once)
        if (currentTarget != null && !isFleeing && !isChasing) {
            if (shouldFleeFromCombat(currentTarget)) {
                isFleeing = true;
                return;
            }

            // Check if target is fleeing and decide to chase
            if (isTargetFleeing(currentTarget) && shouldChaseFleeingTarget(currentTarget)) {
                isChasing = true;
                chaseTarget = currentTarget;
                return;
            }
        }

        // Normal combat maneuvers (only when not fleeing or chasing)
        if (currentTarget != null) {
            performNormalCombatManeuvers(currentTarget);
        }
    }

    /**
     * Performs normal combat maneuvers when not in persistent flee/chase states
     */
    private void performNormalCombatManeuvers(Ship target) {
        // Constants for combat movement
        final double COMBAT_SPEED_FACTOR = 0.8; // Combat movement speed
        final double PLANET_DANGER_RADIUS = 120; // Increased - Distance to consider planets dangerous
        final double EMERGENCY_PLANET_RADIUS = 65; // Increased - Emergency avoidance distance
        final double PREDICTION_TIME = 40; // Predict planet positions this many ticks ahead

        // Calculate direction to enemy
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double distanceToEnemy = Math.hypot(dx, dy);

        if (distanceToEnemy == 0)
            return;

        // Normalize direction to enemy
        dx /= distanceToEnemy;
        dy /= distanceToEnemy;

        // PRIORITY 1: Check for incoming planets and emergency avoidance
        double emergencyAvoidX = 0, emergencyAvoidY = 0;
        double planetThreatLevel = 0;

        List<Planet> allPlanets = operator.getGame().getPlanets();
        for (Planet planet : allPlanets) {
            double currentPlanetDistance = Math.hypot(planet.getX() - x, planet.getY() - y);

            if (currentPlanetDistance < PLANET_DANGER_RADIUS) {
                // Calculate planet's movement direction if it's moving
                double planetVelX = 0, planetVelY = 0;
                if (planet.getOrbitalSpeed() != 0) {
                    // Estimate planet velocity based on orbital motion
                    double orbitalAngle = planet.getOrbitalAngle();
                    if (planet.isVerticalOrbit()) {
                        planetVelX = -planet.getSemiMinorAxis() * planet.getOrbitalSpeed() * Math.sin(orbitalAngle);
                        planetVelY = planet.getSemiMajorAxis() * planet.getOrbitalSpeed() * Math.cos(orbitalAngle);
                    } else {
                        planetVelX = -planet.getSemiMajorAxis() * planet.getOrbitalSpeed() * Math.sin(orbitalAngle);
                        planetVelY = planet.getSemiMinorAxis() * planet.getOrbitalSpeed() * Math.cos(orbitalAngle);
                    }
                }

                // Predict multiple future positions to check for collision course
                boolean onCollisionCourse = false;
                double closestApproach = currentPlanetDistance;

                for (int t = 5; t <= PREDICTION_TIME; t += 5) { // Check every 5 ticks up to PREDICTION_TIME
                    double futurePlanetX = planet.getX() + planetVelX * t;
                    double futurePlanetY = planet.getY() + planetVelY * t;

                    // Predict our position if we continue current movement
                    double futureShipX = x + Math.cos(direction) * speed * t;
                    double futureShipY = y + Math.sin(direction) * speed * t;

                    double futureSeparation = Math.hypot(futurePlanetX - futureShipX, futurePlanetY - futureShipY);

                    if (futureSeparation < closestApproach) {
                        closestApproach = futureSeparation;
                    }

                    if (futureSeparation < 40) { // Collision threshold
                        onCollisionCourse = true;
                        break;
                    }
                }

                // Calculate threat based on current distance and future collision risk
                double threatMultiplier = 1.0;
                if (onCollisionCourse) {
                    threatMultiplier = 3.0; // Much higher threat if on collision course
                } else if (closestApproach < currentPlanetDistance) {
                    threatMultiplier = 2.0; // Planet is approaching
                }

                if (currentPlanetDistance < EMERGENCY_PLANET_RADIUS || onCollisionCourse) {
                    // Calculate avoidance strength based on distance and threat
                    double baseAvoidStrength = (EMERGENCY_PLANET_RADIUS - currentPlanetDistance)
                            / EMERGENCY_PLANET_RADIUS;
                    if (baseAvoidStrength < 0)
                        baseAvoidStrength = 0.2; // Minimum avoidance for collision course

                    double avoidStrength = Math.min(1.0, baseAvoidStrength * threatMultiplier);

                    // Add avoidance force (prevent division by zero)
                    if (currentPlanetDistance > 0.001) {
                        emergencyAvoidX += (x - planet.getX()) / currentPlanetDistance * avoidStrength;
                        emergencyAvoidY += (y - planet.getY()) / currentPlanetDistance * avoidStrength;
                    }
                    planetThreatLevel += avoidStrength;
                }
            }
        }

        // Check sun avoidance during combat
        double centralStarX = GameConstants.getGameWidth() / 2.0;
        double centralStarY = GameConstants.getGameHeight() / 2.0;
        double sunDistance = Math.hypot(centralStarX - x, centralStarY - y);
        if (sunDistance < 70 && sunDistance > 0.001) { // Prevent division by zero
            double sunAvoidStrength = (70 - sunDistance) / 70;
            emergencyAvoidX += (x - centralStarX) / sunDistance * sunAvoidStrength;
            emergencyAvoidY += (y - centralStarY) / sunDistance * sunAvoidStrength;
            planetThreatLevel += sunAvoidStrength;
        }

        // Add ship-to-ship collision avoidance during combat
        List<Ship> allShips = operator.getGame().getShips();
        for (Ship otherShip : allShips) {
            if (otherShip == this || otherShip == target)
                continue; // Don't avoid self or current combat target

            double shipDistance = Math.hypot(otherShip.getX() - x, otherShip.getY() - y);
            if (shipDistance < 35 && shipDistance > 0.001) { // Close collision range, prevent division by zero
                double shipAvoidStrength = (35 - shipDistance) / 35 * 0.8;
                emergencyAvoidX += (x - otherShip.getX()) / shipDistance * shipAvoidStrength;
                emergencyAvoidY += (y - otherShip.getY()) / shipDistance * shipAvoidStrength;
                planetThreatLevel += shipAvoidStrength * 0.5; // Lower priority than planets
            }
        }

        // Add strong screen edge avoidance to prevent ships from flying off screen
        double EDGE_BUFFER = 80; // Distance from screen edge to start avoiding
        double EMERGENCY_EDGE_BUFFER = 20; // Very close to edge - emergency avoidance

        EDGE_BUFFER *= 2.5;
        EMERGENCY_EDGE_BUFFER *= 2.5;

        // Check for corner situations first - these need special handling
        boolean inLeftEdge = x < EDGE_BUFFER;
        boolean inRightEdge = x > GameConstants.getGameWidth() - EDGE_BUFFER;
        boolean inTopEdge = y < EDGE_BUFFER;
        boolean inBottomEdge = y > GameConstants.getGameHeight() - EDGE_BUFFER;

        // Handle corners with diagonal escape to prevent getting stuck
        if ((inLeftEdge && inTopEdge) || (inRightEdge && inTopEdge) ||
                (inLeftEdge && inBottomEdge) || (inRightEdge && inBottomEdge)) {

            // Force diagonal movement away from corner
            if (inLeftEdge && inTopEdge) {
                emergencyAvoidX += 1.5; // Strong push right
                emergencyAvoidY += 1.5; // Strong push down
            } else if (inRightEdge && inTopEdge) {
                emergencyAvoidX -= 1.5; // Strong push left
                emergencyAvoidY += 1.5; // Strong push down
            } else if (inLeftEdge && inBottomEdge) {
                emergencyAvoidX += 1.5; // Strong push right
                emergencyAvoidY -= 1.5; // Strong push up
            } else if (inRightEdge && inBottomEdge) {
                emergencyAvoidX -= 1.5; // Strong push left
                emergencyAvoidY -= 1.5; // Strong push up
            }
            planetThreatLevel += 2.0; // Very high priority for corner escape
        } else {
            // Handle individual edges normally
            if (inLeftEdge) {
                double edgeAvoidStrength = (EDGE_BUFFER - x) / EDGE_BUFFER;
                if (x < EMERGENCY_EDGE_BUFFER) {
                    edgeAvoidStrength = 2.0; // Emergency - very strong push
                }
                emergencyAvoidX += edgeAvoidStrength; // Push right
                planetThreatLevel += edgeAvoidStrength * 0.8; // High priority
            }
            if (inRightEdge) {
                double edgeAvoidStrength = (x - (GameConstants.getGameWidth() - EDGE_BUFFER)) / EDGE_BUFFER;
                if (x > GameConstants.getGameWidth() - EMERGENCY_EDGE_BUFFER) {
                    edgeAvoidStrength = 2.0; // Emergency - very strong push
                }
                emergencyAvoidX -= edgeAvoidStrength; // Push left
                planetThreatLevel += edgeAvoidStrength * 0.8; // High priority
            }
            if (inTopEdge) {
                double edgeAvoidStrength = (EDGE_BUFFER - y) / EDGE_BUFFER;
                if (y < EMERGENCY_EDGE_BUFFER) {
                    edgeAvoidStrength = 2.0; // Emergency - very strong push
                }
                emergencyAvoidY += edgeAvoidStrength; // Push down
                planetThreatLevel += edgeAvoidStrength * 0.8; // High priority
            }
            if (inBottomEdge) {
                double edgeAvoidStrength = (y - (GameConstants.getGameHeight() - EDGE_BUFFER)) / EDGE_BUFFER;
                if (y > GameConstants.getGameHeight() - EMERGENCY_EDGE_BUFFER) {
                    edgeAvoidStrength = 2.0; // Emergency - very strong push
                }
                emergencyAvoidY -= edgeAvoidStrength; // Push up
                planetThreatLevel += edgeAvoidStrength * 0.8; // High priority
            }
        }

        // If high threat from planets/sun, prioritize avoidance
        if (planetThreatLevel > 0.5) {
            double avoidLength = Math.hypot(emergencyAvoidX, emergencyAvoidY);
            if (avoidLength > 0) {
                emergencyAvoidX /= avoidLength;
                emergencyAvoidY /= avoidLength;

                // Emergency movement - override combat maneuvers
                double moveX = speed * emergencyAvoidX;
                double moveY = speed * emergencyAvoidY;

                x += moveX;
                y += moveY;

                // Debug: Check emergency avoidance movement for invalid results
                if ((x < 5 && y < 5) || Double.isNaN(x) || Double.isNaN(y)) {
                    // Fix invalid positions
                    if (Double.isNaN(x) || x < 10) {
                        x = Math.max(10, x - moveX); // Revert to old position with safety margin
                        emergencyAvoidX = 0; // Clear bad avoidance vector
                    }
                    if (Double.isNaN(y) || y < 10) {
                        y = Math.max(10, y - moveY); // Revert to old position with safety margin
                        emergencyAvoidY = 0; // Clear bad avoidance vector
                    }
                }

                // Still try to face enemy for shooting when possible
                direction = Math.atan2(dy, dx);
                return;
            }
        }

        // PRIORITY 2: Normal combat maneuvers when not in immediate danger
        // Each ship gets a unique maneuver pattern based on its hash to prevent
        // synchronization
        int shipId = System.identityHashCode(this); // Unique identifier for this ship
        long time = System.currentTimeMillis();

        // Ships change maneuvers at different intervals to prevent synchronized
        // movement
        int maneuverDuration = 2500 + (shipId % 2000); // 2.5-4.5 second intervals, unique per ship
        int maneuverType = (int) ((time / maneuverDuration + shipId) % 7); // 7 different maneuvers

        double moveX = 0, moveY = 0;

        switch (maneuverType) {
            case 0: // Clockwise circling
                moveX = dy * 0.9;
                moveY = -dx * 0.9;
                break;

            case 1: // Counter-clockwise circling
                moveX = -dy * 0.9;
                moveY = dx * 0.9;
                break;

            case 2: // Approach and retreat (strafing)
                if (distanceToEnemy > GameConstants.getCombatEngagementDistance()) {
                    moveX = dx * 0.6 + dy * 0.4;
                    moveY = dy * 0.6 - dx * 0.4;
                } else {
                    moveX = -dx * 0.3 + dy * 0.7;
                    moveY = -dy * 0.3 - dx * 0.7;
                }
                break;

            case 3: // Figure-8 / weaving maneuver
                double waveTime = (time % 2000) / 2000.0 * Math.PI * 2;
                double waveOffset = Math.sin(waveTime + shipId) * 0.6; // Unique phase per ship
                moveX = dy * waveOffset - dx * 0.2;
                moveY = -dx * waveOffset - dy * 0.2;
                break;

            case 4: // Spiral maneuver (expanding/contracting circle)
                double spiralTime = (time % 4000) / 4000.0 * Math.PI * 2;
                double spiralRadius = 0.5 + 0.4 * Math.sin(spiralTime + shipId);
                moveX = dy * spiralRadius + dx * (0.7 - spiralRadius);
                moveY = -dx * spiralRadius + dy * (0.7 - spiralRadius);
                break;

            case 5: // Hit and run (quick approach then retreat)
                double cycleTime = (time + shipId * 100) % 3000; // 3-second cycle, offset per ship
                if (cycleTime < 1000) {
                    // Aggressive approach
                    moveX = dx * 0.8 + dy * 0.3;
                    moveY = dy * 0.8 - dx * 0.3;
                } else {
                    // Retreat while strafing
                    moveX = -dx * 0.6 + dy * 0.8;
                    moveY = -dy * 0.6 - dx * 0.8;
                }
                break;

            case 6: // Zigzag/evasive pattern
                double zigzagTime = (time + shipId * 50) % 1500; // 1.5-second zigzag, offset per ship
                double zigzagDirection = (zigzagTime < 750) ? 1.0 : -1.0;
                moveX = dy * zigzagDirection * 0.8 + dx * 0.2;
                moveY = -dx * zigzagDirection * 0.8 + dy * 0.2;
                break;
        }

        // Add slight avoidance for nearby obstacles (but not emergency level)
        moveX += emergencyAvoidX * 0.3;
        moveY += emergencyAvoidY * 0.3;

        // Normalize and apply movement
        double moveLength = Math.hypot(moveX, moveY);
        if (moveLength > 0) {
            moveX /= moveLength;
            moveY /= moveLength;

            // Store old position for debugging
            double oldX = x;
            double oldY = y;

            // Apply combat movement
            x += speed * COMBAT_SPEED_FACTOR * moveX;
            y += speed * COMBAT_SPEED_FACTOR * moveY;

            // Debug: Check for invalid movement
            if ((x < 5 && y < 5) || Double.isNaN(x) || Double.isNaN(y)) {
                // Fix invalid positions
                if (Double.isNaN(x) || x < 10)
                    x = Math.max(10, oldX);
                if (Double.isNaN(y) || y < 10)
                    y = Math.max(10, oldY);
            }

            // Always face the enemy for accurate shooting
            direction = Math.atan2(dy, dx);
        }
    }

    /**
     * Checks if ship can exit flee state (safe distance from enemies)
     */
    private boolean canExitFleeState() {
        // Exit flee state when far enough from all enemies or at destination
        double distanceToDestination = Math.hypot(destination.getX() - x, destination.getY() - y);
        if (distanceToDestination < 30) {
            return true; // Reached destination safely
        }

        // Check if all enemies are far away
        List<Ship> allShips = operator.getGame().getShips();
        for (Ship ship : allShips) {
            if (ship.getOperator() != this.operator) { // Enemy ship
                double enemyDistance = Math.hypot(ship.getX() - x, ship.getY() - y);
                if (enemyDistance < 100) { // Still too close to enemies
                    return false;
                }
            }
        }
        return true; // All enemies are far away
    }

    /**
     * Checks if ship can exit chase state
     */
    private boolean canExitChaseState() {
        if (chaseTarget == null || chaseTarget.isDestroyed()) {
            return true; // Target is gone
        }

        // Exit if target reached its destination
        double targetToDestDistance = Math.hypot(chaseTarget.getDestination().getX() - chaseTarget.getX(),
                chaseTarget.getDestination().getY() - chaseTarget.getY());
        if (targetToDestDistance < 30) {
            return true; // Target reached its destination
        }

        // Exit if target is too far away and we're too far from our own destination
        double chaseDistance = Math.hypot(chaseTarget.getX() - x, chaseTarget.getY() - y);
        double homeDistance = Math.hypot(destination.getX() - x, destination.getY() - y);

        if (chaseDistance > 150 && homeDistance > chaseDistance * 1.5) {
            return true; // Target too far, our mission is more important
        }

        return false;
    }

    /**
     * Determines if this ship should flee from combat based on tactical situation
     */
    private boolean shouldFleeFromCombat(Ship target) {
        // Count nearby enemy and friendly ships
        int nearbyEnemies = 0;
        int nearbyFriendlies = 0;
        double scanRadius = 150; // Radius to check for other ships

        List<Ship> allShips = operator.getGame().getShips();
        for (Ship ship : allShips) {
            if (ship == this)
                continue;

            double distance = Math.hypot(ship.getX() - x, ship.getY() - y);
            if (distance < scanRadius) {
                if (ship.getOperator() == this.operator) {
                    nearbyFriendlies++;
                } else {
                    nearbyEnemies++;
                }
            }
        }

        // Factors that influence fleeing decision
        boolean outnumbered = nearbyEnemies > nearbyFriendlies + 1; // +1 includes self
        boolean lowHealth = health < getMaxHealth() * 0.4; // Below 40% health
        boolean farFromSupport = nearbyFriendlies == 0 && nearbyEnemies >= 2;

        // Bot ships are more likely to flee when tactical situation is poor
        if (operator instanceof Bot) {
            return outnumbered || (lowHealth && nearbyEnemies >= 1) || farFromSupport;
        }

        // Player ships are more aggressive but will still flee when severely
        // outnumbered
        return (outnumbered && nearbyEnemies >= 3) || (lowHealth && nearbyEnemies >= 2);
    }

    /**
     * Checks if the target ship appears to be fleeing
     */
    private boolean isTargetFleeing(Ship target) {
        // Calculate if target is moving away from us
        double targetDx = target.getX() - x;
        double targetDy = target.getY() - y;
        double targetAngle = Math.atan2(targetDy, targetDx);

        // Check if target's movement direction is roughly opposite to us
        double angleDifference = Math.abs(target.getDirection() - targetAngle);
        if (angleDifference > Math.PI) {
            angleDifference = 2 * Math.PI - angleDifference;
        }

        // Target is fleeing if moving roughly away from us (within 60 degrees of
        // opposite direction)
        return angleDifference > Math.PI * 2 / 3;
    }

    /**
     * Decides whether to chase a fleeing target or return to original mission
     */
    private boolean shouldChaseFleeingTarget(Ship target) {
        // Factors for chase decision
        double distanceToTarget = Math.hypot(target.getX() - x, target.getY() - y);
        double distanceToDestination = Math.hypot(destination.getX() - x, destination.getY() - y);

        // Don't chase if target is much faster and getting away
        boolean targetEscaping = target.getSpeed() > speed * 1.2 && distanceToTarget > 100;

        // Don't chase if our destination is much closer and more important
        boolean destinationPriority = distanceToDestination < distanceToTarget * 0.6;

        // Bot behavior: more tactical decision making
        if (operator instanceof Bot) {
            // Don't chase if target is nearly destroyed anyway or if destination is
            // priority
            boolean targetAlmostDead = target.getHealth() < target.getMaxHealth() * 0.2;
            return !targetEscaping && !destinationPriority && !targetAlmostDead;
        }

        // Player ships are more aggressive in chasing
        return !targetEscaping && distanceToTarget < 120;
    }

    /**
     * Performs fleeing maneuver - move away from enemy while avoiding obstacles
     */
    private void performFleeManeuver(Ship enemy) {
        final double FLEE_SPEED_FACTOR = 1.1; // Slightly faster when fleeing

        // Primary direction: away from enemy
        double enemyDx = enemy.getX() - x;
        double enemyDy = enemy.getY() - y;
        double enemyDistance = Math.hypot(enemyDx, enemyDy);

        if (enemyDistance == 0)
            return;

        // Normalize enemy direction
        enemyDx /= enemyDistance;
        enemyDy /= enemyDistance;

        // Flee direction (away from enemy)
        double fleeX = -enemyDx;
        double fleeY = -enemyDy;

        // Add obstacle avoidance while fleeing
        double avoidanceX = 0, avoidanceY = 0;

        // Sun avoidance
        double centralStarX = GameConstants.getGameWidth() / 2.0;
        double centralStarY = GameConstants.getGameHeight() / 2.0;
        double sunDistance = Math.hypot(centralStarX - x, centralStarY - y);
        if (sunDistance < 80) {
            avoidanceX += (x - centralStarX) / sunDistance;
            avoidanceY += (y - centralStarY) / sunDistance;
        }

        // Planet avoidance
        List<Planet> allPlanets = operator.getGame().getPlanets();
        for (Planet planet : allPlanets) {
            double planetDistance = Math.hypot(planet.getX() - x, planet.getY() - y);
            if (planetDistance < 60) {
                avoidanceX += (x - planet.getX()) / planetDistance * 0.5;
                avoidanceY += (y - planet.getY()) / planetDistance * 0.5;
            }
        }

        // Strong screen edge avoidance during flee
        final double FLEE_EDGE_BUFFER = 70;
        if (x < FLEE_EDGE_BUFFER) {
            avoidanceX += (FLEE_EDGE_BUFFER - x) / FLEE_EDGE_BUFFER * 1.5; // Push right
        }
        if (x > GameConstants.getGameWidth() - FLEE_EDGE_BUFFER) {
            avoidanceX -= (x - (GameConstants.getGameWidth() - FLEE_EDGE_BUFFER)) / FLEE_EDGE_BUFFER * 1.5; // Push left
        }
        if (y < FLEE_EDGE_BUFFER) {
            avoidanceY += (FLEE_EDGE_BUFFER - y) / FLEE_EDGE_BUFFER * 1.5; // Push down
        }
        if (y > GameConstants.getGameHeight() - FLEE_EDGE_BUFFER) {
            avoidanceY -= (y - (GameConstants.getGameHeight() - FLEE_EDGE_BUFFER)) / FLEE_EDGE_BUFFER * 1.5; // Push up
        }

        // Combine flee direction with avoidance
        fleeX += avoidanceX * 0.4;
        fleeY += avoidanceY * 0.4;

        // Normalize and apply movement
        double fleeLength = Math.hypot(fleeX, fleeY);
        if (fleeLength > 0) {
            fleeX /= fleeLength;
            fleeY /= fleeLength;

            x += speed * FLEE_SPEED_FACTOR * fleeX;
            y += speed * FLEE_SPEED_FACTOR * fleeY;
            direction = Math.atan2(fleeY, fleeX);
        }
    }

    /**
     * Performs chase maneuver - aggressively pursue fleeing target
     */
    private void performChaseManeuver(Ship target) {
        final double CHASE_SPEED_FACTOR = 1.05; // Slightly faster when chasing

        // Predict where target will be
        double targetDx = Math.cos(target.getDirection()) * target.getSpeed() * 10; // Predict 10 ticks ahead
        double targetDy = Math.sin(target.getDirection()) * target.getSpeed() * 10;
        double predictedX = target.getX() + targetDx;
        double predictedY = target.getY() + targetDy;

        // Move towards predicted position
        double chaseDx = predictedX - x;
        double chaseDy = predictedY - y;
        double chaseDistance = Math.hypot(chaseDx, chaseDy);

        if (chaseDistance > 0) {
            chaseDx /= chaseDistance;
            chaseDy /= chaseDistance;

            // Simple obstacle avoidance during chase
            double avoidanceX = 0, avoidanceY = 0;

            // Sun avoidance
            double centralStarX = GameConstants.getGameWidth() / 2.0;
            double centralStarY = GameConstants.getGameHeight() / 2.0;
            double sunDistance = Math.hypot(centralStarX - x, centralStarY - y);
            if (sunDistance < 60) {
                avoidanceX += (x - centralStarX) / sunDistance;
                avoidanceY += (y - centralStarY) / sunDistance;
            }

            // Blend chase with avoidance
            chaseDx += avoidanceX * 0.3;
            chaseDy += avoidanceY * 0.3;

            // Normalize and apply
            double finalLength = Math.hypot(chaseDx, chaseDy);
            if (finalLength > 0) {
                chaseDx /= finalLength;
                chaseDy /= finalLength;

                x += speed * CHASE_SPEED_FACTOR * chaseDx;
                y += speed * CHASE_SPEED_FACTOR * chaseDy;
                direction = Math.atan2(target.getY() - y, target.getX() - x); // Face the target
            }
        }
    }

    // Add maxHealth getter for flee logic
    private int getMaxHealth() {
        return GameConstants.getDefaultShipHealth(); // Assuming all ships have same max health
    }

    /**
     * Calculates a smart path that includes initial launch direction and strong
     * obstacle avoidance
     * Ships launch away from their origin planet and navigate around obstacles
     * Includes smoothing to prevent twitching behavior
     */
    private double calculateSmartPath(double desiredAngle, double targetX, double targetY) {
        // Constants for pathfinding
        final double LAUNCH_DISTANCE = 50; // Distance before switching to target-seeking
        final double SUN_AVOIDANCE_RADIUS = 80; // Much larger radius for sun avoidance
        final double PLANET_AVOIDANCE_RADIUS = 60; // Larger radius for planet avoidance
        final double EMERGENCY_AVOIDANCE_RADIUS = 35; // Very close - emergency steering
        final double SMOOTHING_FACTOR = 0.15; // How much to smooth direction changes (0.0 to 1.0)
        final double FINAL_APPROACH_DISTANCE = 40; // Switch to direct path when this close to target

        // Central star position
        double centralStarX = GameConstants.getGameWidth() / 2.0;
        double centralStarY = GameConstants.getGameHeight() / 2.0;

        // Check distance from origin to determine launch phase
        double distanceFromOrigin = (origin != null) ? Math.hypot(origin.getX() - x, origin.getY() - y)
                : Double.MAX_VALUE;

        // Check distance to target - if very close, use direct approach to prevent
        // swaying
        double distanceToTarget = Math.hypot(targetX - x, targetY - y);
        if (distanceToTarget < FINAL_APPROACH_DISTANCE) {
            return desiredAngle; // Direct path when close to target
        }

        // Phase 1: Launch phase - move away from origin planet
        if (distanceFromOrigin < LAUNCH_DISTANCE && origin != null) {
            // Calculate launch direction (away from origin, towards general target
            // direction)
            double awayFromOriginX = x - origin.getX();
            double awayFromOriginY = y - origin.getY();
            double awayLength = Math.hypot(awayFromOriginX, awayFromOriginY);

            if (awayLength > 0) {
                awayFromOriginX /= awayLength;
                awayFromOriginY /= awayLength;
            }

            // Blend launch direction with target direction (70% away, 30% towards target)
            double targetDirX = Math.cos(desiredAngle);
            double targetDirY = Math.sin(desiredAngle);

            double launchX = awayFromOriginX * 0.7 + targetDirX * 0.3;
            double launchY = awayFromOriginY * 0.7 + targetDirY * 0.3;

            return Math.atan2(launchY, launchX);
        }

        // Phase 2: Navigation phase with obstacle avoidance
        double steeringX = Math.cos(desiredAngle);
        double steeringY = Math.sin(desiredAngle);

        // Track total avoidance force to prevent over-correction
        double totalAvoidanceForce = 0;

        // Check for sun collision
        double sunDistance = Math.hypot(centralStarX - x, centralStarY - y);
        if (sunDistance < SUN_AVOIDANCE_RADIUS && sunDistance > 0.001) { // Prevent division by zero
            // Strong avoidance force from sun
            double avoidanceStrength = Math.pow((SUN_AVOIDANCE_RADIUS - sunDistance) / SUN_AVOIDANCE_RADIUS, 0.5);
            if (sunDistance < EMERGENCY_AVOIDANCE_RADIUS) {
                avoidanceStrength = 1.5; // Emergency - extra strong avoidance
            }

            double sunAvoidX = (x - centralStarX) / sunDistance;
            double sunAvoidY = (y - centralStarY) / sunDistance;

            // Apply strong steering away from sun
            steeringX += sunAvoidX * avoidanceStrength;
            steeringY += sunAvoidY * avoidanceStrength;
            totalAvoidanceForce += avoidanceStrength;
        }

        // Check for planet collisions
        List<Planet> allPlanets = operator.getGame().getPlanets();
        for (Planet planet : allPlanets) {
            if (planet == destination || planet == origin) {
                continue; // Don't avoid our origin or destination planet
            }

            double planetDistance = Math.hypot(planet.getX() - x, planet.getY() - y);
            if (planetDistance < PLANET_AVOIDANCE_RADIUS) {
                // Enhanced prediction for regular pathfinding too
                boolean onCollisionCourse = false;

                if (planet.getOrbitalSpeed() != 0) {
                    // Calculate planet velocity
                    double orbitalAngle = planet.getOrbitalAngle();
                    double planetVelX, planetVelY;

                    if (planet.isVerticalOrbit()) {
                        planetVelX = -planet.getSemiMinorAxis() * planet.getOrbitalSpeed() * Math.sin(orbitalAngle);
                        planetVelY = planet.getSemiMajorAxis() * planet.getOrbitalSpeed() * Math.cos(orbitalAngle);
                    } else {
                        planetVelX = -planet.getSemiMajorAxis() * planet.getOrbitalSpeed() * Math.sin(orbitalAngle);
                        planetVelY = planet.getSemiMinorAxis() * planet.getOrbitalSpeed() * Math.cos(orbitalAngle);
                    }

                    // Quick collision check for next 25 ticks
                    for (int t = 5; t <= 25; t += 5) {
                        double futurePlanetX = planet.getX() + planetVelX * t;
                        double futurePlanetY = planet.getY() + planetVelY * t;
                        double futureShipX = x + Math.cos(direction) * speed * t;
                        double futureShipY = y + Math.sin(direction) * speed * t;

                        if (Math.hypot(futurePlanetX - futureShipX, futurePlanetY - futureShipY) < 45) {
                            onCollisionCourse = true;
                            break;
                        }
                    }
                }

                double avoidanceStrength = Math
                        .pow((PLANET_AVOIDANCE_RADIUS - planetDistance) / PLANET_AVOIDANCE_RADIUS, 0.5);
                if (planetDistance < EMERGENCY_AVOIDANCE_RADIUS || onCollisionCourse) {
                    avoidanceStrength = Math.min(1.2, avoidanceStrength * 1.5); // Stronger avoidance
                }

                // Prevent division by zero
                if (planetDistance > 0.001) {
                    double planetAvoidX = (x - planet.getX()) / planetDistance;
                    double planetAvoidY = (y - planet.getY()) / planetDistance;

                    // Apply steering away from planet
                    steeringX += planetAvoidX * avoidanceStrength * 0.8;
                    steeringY += planetAvoidY * avoidanceStrength * 0.8;
                    totalAvoidanceForce += avoidanceStrength * 0.8;
                }
            }
        }

        // Check for pinch points - only when not already heavily avoiding
        if (totalAvoidanceForce < 0.3) { // Only check pinch points when not in heavy avoidance mode
            boolean inPinchPoint = detectPinchPoint(steeringX, steeringY, allPlanets);

            if (inPinchPoint) {
                // Calculate avoidance direction for pinch point
                double[] avoidanceVector = calculatePinchAvoidance(allPlanets);

                // Apply gentle pinch avoidance - much weaker than obstacle avoidance
                steeringX += avoidanceVector[0] * 0.3;
                steeringY += avoidanceVector[1] * 0.3;
                totalAvoidanceForce += 0.2;
            }
        }

        // Add ship-to-ship collision avoidance for regular pathfinding
        List<Ship> allShips = operator.getGame().getShips();
        for (Ship otherShip : allShips) {
            if (otherShip == this)
                continue; // Don't avoid self

            double shipDistance = Math.hypot(otherShip.getX() - x, otherShip.getY() - y);
            if (shipDistance < 25 && shipDistance > 0.001) { // Close collision range, prevent division by zero
                double shipAvoidStrength = (25 - shipDistance) / 25 * 0.4; // Gentler avoidance during regular movement
                double shipAvoidX = (x - otherShip.getX()) / shipDistance;
                double shipAvoidY = (y - otherShip.getY()) / shipDistance;

                steeringX += shipAvoidX * shipAvoidStrength;
                steeringY += shipAvoidY * shipAvoidStrength;
                totalAvoidanceForce += shipAvoidStrength * 0.3;
            }
        }

        // Add screen edge avoidance to regular pathfinding
        final double EDGE_BUFFER = 50; // Distance from screen edge to start avoiding
        if (x < EDGE_BUFFER) {
            double edgeAvoidStrength = (EDGE_BUFFER - x) / EDGE_BUFFER * 0.5;
            steeringX += edgeAvoidStrength; // Push right
            totalAvoidanceForce += edgeAvoidStrength * 0.3;
        }
        if (x > GameConstants.getGameWidth() - EDGE_BUFFER) {
            double edgeAvoidStrength = (x - (GameConstants.getGameWidth() - EDGE_BUFFER)) / EDGE_BUFFER * 0.5;
            steeringX -= edgeAvoidStrength; // Push left
            totalAvoidanceForce += edgeAvoidStrength * 0.3;
        }
        if (y < EDGE_BUFFER) {
            double edgeAvoidStrength = (EDGE_BUFFER - y) / EDGE_BUFFER * 0.5;
            steeringY += edgeAvoidStrength; // Push down
            totalAvoidanceForce += edgeAvoidStrength * 0.3;
        }
        if (y > GameConstants.getGameHeight() - EDGE_BUFFER) {
            double edgeAvoidStrength = (y - (GameConstants.getGameHeight() - EDGE_BUFFER)) / EDGE_BUFFER * 0.5;
            steeringY -= edgeAvoidStrength; // Push up
            totalAvoidanceForce += edgeAvoidStrength * 0.3;
        }

        // Check for invalid steering vectors that can cause NaN
        if (Double.isNaN(steeringX) || Double.isNaN(steeringY)) {
            return desiredAngle; // Fallback to desired angle
        }

        // Check for zero vector (but don't treat as error - just use desired direction)
        if (steeringX == 0 && steeringY == 0) {
            return desiredAngle; // Use desired angle when no steering is needed
        }

        // Calculate new angle
        double newAngle = Math.atan2(steeringY, steeringX);

        // Apply smoothing to prevent twitching (blend with current direction)
        if (totalAvoidanceForce < 0.5) { // Only smooth when not in heavy avoidance
            double currentDirX = Math.cos(direction);
            double currentDirY = Math.sin(direction);
            double newDirX = Math.cos(newAngle);
            double newDirY = Math.sin(newAngle);

            // Blend current direction with new direction
            double smoothedDirX = currentDirX * (1 - SMOOTHING_FACTOR) + newDirX * SMOOTHING_FACTOR;
            double smoothedDirY = currentDirY * (1 - SMOOTHING_FACTOR) + newDirY * SMOOTHING_FACTOR;

            // Check for zero vector after smoothing
            if (smoothedDirX == 0 && smoothedDirY == 0) {
                return newAngle;
            }

            newAngle = Math.atan2(smoothedDirY, smoothedDirX);
        }

        // Final check for NaN angle
        if (Double.isNaN(newAngle)) {
            return desiredAngle;
        }

        return newAngle;
    }

    /**
     * Detects if the ship is heading into a pinch point (narrow space between
     * planets)
     * More conservative detection to reduce wobbling
     */
    private boolean detectPinchPoint(double steeringX, double steeringY, List<Planet> allPlanets) {
        final double LOOKAHEAD_DISTANCE = 150; // Shorter lookahead to be less aggressive
        final double PINCH_THRESHOLD = 35; // Tighter threshold - only for genuine tight spaces

        // Normalize steering direction
        double steeringLength = Math.hypot(steeringX, steeringY);
        if (steeringLength == 0)
            return false;

        double dirX = steeringX / steeringLength;
        double dirY = steeringY / steeringLength;

        // Only check the furthest point to avoid over-sensitivity
        double checkX = x + dirX * LOOKAHEAD_DISTANCE;
        double checkY = y + dirY * LOOKAHEAD_DISTANCE;

        // Count nearby planets at this check point
        int nearbyPlanets = 0;
        double minClearance = Double.MAX_VALUE;

        for (Planet planet : allPlanets) {
            if (planet == destination || planet == origin)
                continue;

            double distToPlanet = Math.hypot(planet.getX() - checkX, planet.getY() - checkY);
            double effectiveRadius = GameConstants.getPlanetSize() + 15; // Smaller safety buffer

            if (distToPlanet < effectiveRadius * 1.8) { // Smaller influence range
                nearbyPlanets++;
                minClearance = Math.min(minClearance, distToPlanet - effectiveRadius);
            }
        }

        // Check for sun proximity too
        double centralStarX = GameConstants.getGameWidth() / 2.0;
        double centralStarY = GameConstants.getGameHeight() / 2.0;
        double distToSun = Math.hypot(centralStarX - checkX, centralStarY - checkY);
        if (distToSun < 50) { // Closer threshold for sun
            nearbyPlanets++;
            minClearance = Math.min(minClearance, distToSun - 25);
        }

        // Require at least 3 obstacles or very tight clearance for genuine pinch point
        return (nearbyPlanets >= 3) || (nearbyPlanets >= 2 && minClearance < PINCH_THRESHOLD);
    }

    /**
     * Calculates simple avoidance direction when in a pinch point
     * Simplified to reduce wobbling - just finds general direction with most space
     */
    private double[] calculatePinchAvoidance(List<Planet> allPlanets) {
        double bestAvoidX = 0;
        double bestAvoidY = 0;
        double maxClearance = 0;

        // Check only 8 cardinal and intercardinal directions for simplicity
        for (int angle = 0; angle < 360; angle += 45) {
            double testAngle = Math.toRadians(angle);
            double testDirX = Math.cos(testAngle);
            double testDirY = Math.sin(testAngle);

            // Calculate minimum clearance in this direction (simpler calculation)
            double minClearance = Double.MAX_VALUE;

            // Check against all planets
            for (Planet planet : allPlanets) {
                if (planet == destination || planet == origin)
                    continue;

                double distToPlanet = Math.hypot(planet.getX() - x, planet.getY() - y);
                minClearance = Math.min(minClearance, distToPlanet);
            }

            // Check against sun
            double centralStarX = GameConstants.getGameWidth() / 2.0;
            double centralStarY = GameConstants.getGameHeight() / 2.0;
            double distToSun = Math.hypot(centralStarX - x, centralStarY - y);
            minClearance = Math.min(minClearance, distToSun);

            // Simple scoring - just find direction with most clearance
            if (minClearance > maxClearance) {
                maxClearance = minClearance;
                bestAvoidX = testDirX;
                bestAvoidY = testDirY;
            }
        }

        return new double[] { bestAvoidX, bestAvoidY };
    }

    public void takeDamage(int damage) {
        // Check if shield ability is active for player ships
        if (operator instanceof Player && game != null) {
            if (GameConstants.arePlayerShipsInvincible()) {
                return; // No damage when invincibility is enabled
            }
            if (game.getAbilityManager().isShieldActive()) {
                return; // No damage when shield is active
            }

            // Check if unstoppable ships ability is active for player ships
            if (game.getAbilityManager().isUnstoppableShipsActive()) {
                return; // No damage when unstoppable ships is active
            }
        }

        boolean wasAlive = this.health > 0;
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }

        // Create explosion when ship dies
        if (wasAlive && this.health <= 0 && game != null) {
            createExplosion(Explosion.ExplosionType.SHIP_DESTRUCTION);
        }
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isDestroyed() {
        return this.health <= 0;
    }

    public Operator getOperator() {
        return operator;
    }

    public Planet getOrigin() {
        return origin;
    }

    public Planet getDestination() {
        return destination;
    }

    public double getSpeed() {
        return speed;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public void setTarget(Planet newDestination) {
        this.destination = newDestination;
    }

    public void setStationary(boolean stationary) {
        this.stationary = stationary;
    }

    // Combat-related getters - delegate to CombatManager through Game
    public boolean isInCombat() {
        return operator.getGame().getCombatManager().isInCombat(this);
    }

    public Ship getCombatTarget() {
        return operator.getGame().getCombatManager().getCombatTarget(this);
    }

    public boolean isStationary() {
        return stationary || operator.getGame().getCombatManager().isStationary(this);
    }

    public boolean isMissile() {
        return isMissile;
    }

}
