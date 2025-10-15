package home.game.combat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import home.game.Game;
import home.game.GameConstants;
import home.game.Projectile;
import home.game.Ship;

public class CombatManager {

    private Game game;
    private Map<Ship, CombatState> combatStates;

    public CombatManager(Game game) {
        this.game = game;
        this.combatStates = new HashMap<>();
    }

    /**
     * Updates combat for all ships
     */
    public void updateCombat() {
        List<Ship> allShips = game.getShips();

        // First pass: validate existing combat states and find new targets
        for (Ship ship : allShips) {
            if (ship.isDestroyed() || ship.isMissile()) {
                continue; // Skip destroyed ships and missiles
            }

            CombatState state = getOrCreateCombatState(ship);

            // Validate current combat state
            if (state.inCombat && !isValidCombatState(ship, state)) {
                exitCombat(ship, state);
            }

            // Look for new targets if not in combat
            if (!state.inCombat) {
                Ship target = findNearbyEnemyShip(ship);
                if (target != null) {
                    enterCombat(ship, target, state);
                }
            }

            // Handle combat behavior
            if (state.inCombat) {
                handleCombat(ship, state);
            }
        }
    }

    /**
     * Gets or creates a combat state for a ship
     */
    private CombatState getOrCreateCombatState(Ship ship) {
        return combatStates.computeIfAbsent(ship, k -> new CombatState());
    }

    /**
     * Finds nearby enemy ships that are attacking the given ship's origin planet
     */
    private Ship findNearbyEnemyShip(Ship ship) {
        List<Ship> allShips = game.getShips();
        Ship closestEnemy = null;
        double closestDistance = GameConstants.getCombatEngagementDistance();

        for (Ship otherShip : allShips) {
            // Only target enemy ships that are attacking our origin planet
            if (otherShip != ship &&
                    otherShip.getOperator() != ship.getOperator() &&
                    !otherShip.isDestroyed() &&
                    !otherShip.isMissile() &&
                    otherShip.getDestination() == ship.getOrigin()) {

                double distance = Math.hypot(otherShip.getX() - ship.getX(), otherShip.getY() - ship.getY());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEnemy = otherShip;
                }
            }
        }

        return closestEnemy;
    }

    /**
     * Validates if a ship's combat state is still valid
     */
    private boolean isValidCombatState(Ship ship, CombatState state) {
        if (!state.inCombat) {
            return true;
        }

        if (state.combatTarget == null || state.combatTarget.isDestroyed()) {
            return false;
        }

        // Check if target is still a valid threat
        if (!isValidCombatTarget(ship, state.combatTarget)) {
            return false;
        }

        // Check distance
        double distance = Math.hypot(state.combatTarget.getX() - ship.getX(),
                state.combatTarget.getY() - ship.getY());
        if (distance > GameConstants.getCombatDisengagementDistance()) {
            return false;
        }

        // Only exit if target is clearly moving away (more conservative check)
        CombatState targetState = combatStates.get(state.combatTarget);
        if (targetState != null && !targetState.inCombat) {
            // Only exit if target is far away AND moving away from us
            if (distance > GameConstants.getCombatEngagementDistance()
                    && !isTargetApproaching(ship, state.combatTarget)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a ship is a valid combat target
     */
    private boolean isValidCombatTarget(Ship ship, Ship target) {
        if (target == null || target.isDestroyed()) {
            return false;
        }
        if (!game.getShips().contains(target)) {
            return false; // Target is no longer in the game
        }

        // Target must be from different operator and attacking our origin
        return target.getOperator() != ship.getOperator() &&
                target.getDestination() == ship.getOrigin();
    }

    /**
     * Checks if target is approaching the ship or its origin
     */
    private boolean isTargetApproaching(Ship ship, Ship target) {
        if (target == null) {
            return false;
        }

        // Check if target is moving toward our origin planet
        if (target.getDestination() == ship.getOrigin()) {
            return true;
        }

        // If target is stationary (in combat), consider it as approaching
        CombatState targetState = combatStates.get(target);
        if (targetState != null && targetState.inCombat) {
            return true;
        }

        // Calculate target's direction relative to us
        double targetDirection = target.getDirection();
        double angleToUs = Math.atan2(ship.getY() - target.getY(), ship.getX() - target.getX());

        // Check if target is moving roughly toward us (within 120 degrees for more
        // leniency)
        double angleDifference = Math.abs(targetDirection - angleToUs);
        if (angleDifference > Math.PI) {
            angleDifference = 2 * Math.PI - angleDifference;
        }

        return angleDifference < (2 * Math.PI / 3); // Within 120 degrees = approaching (more lenient)
    }

    /**
     * Enters combat mode for a ship
     */
    private void enterCombat(Ship ship, Ship target, CombatState state) {
        // Safety check
        if (target == null || target.isDestroyed() || !isValidCombatTarget(ship, target)) {
            return;
        }

        state.inCombat = true;
        state.combatTarget = target;

        // Face the enemy
        double dx = target.getX() - ship.getX();
        double dy = target.getY() - ship.getY();
        ship.setDirection(Math.atan2(dy, dx));
        ship.setStationary(true);
    }

    /**
     * Exits combat mode for a ship
     */
    private void exitCombat(Ship ship, CombatState state) {
        state.inCombat = false;
        state.combatTarget = null;
        state.lastShotTime = 0;
        ship.setStationary(false);

        // Ensure ship resumes movement toward its destination
        if (ship.getDestination() != null) {
            // Ship should automatically resume movement in its tick() method
            // since stationary is now false
        }
    }

    /**
     * Handles combat behavior for a ship
     */
    private void handleCombat(Ship ship, CombatState state) {
        if (!isValidCombatState(ship, state)) {
            exitCombat(ship, state);
            return;
        }

        if (state.combatTarget == null) {
            exitCombat(ship, state);
            return;
        }

        // Ships face their target during normal combat, but not during evasive
        // maneuvers
        // The facing direction check in isShipFacingTarget will handle whether they can
        // shoot
        double dx = state.combatTarget.getX() - ship.getX();
        double dy = state.combatTarget.getY() - ship.getY();
        double targetDirection = Math.atan2(dy, dx);

        // Only force direction change if ship is stationary in combat
        // Moving ships (fleeing/chasing/maneuvering) keep their movement direction
        if (ship.isStationary()) {
            ship.setDirection(targetDirection);
        }

        // Try to shoot - but only if ship is actually facing the target
        if (state.combatTarget != null && !state.combatTarget.isDestroyed()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - state.lastShotTime >= GameConstants.getShipFireRate()) {
                // Check if ship is facing the target before allowing shooting
                if (isShipFacingTarget(ship, state.combatTarget)) {
                    fireProjectile(ship, state.combatTarget);
                    state.lastShotTime = currentTime;
                }
            }
        }
    }

    /**
     * Fires a projectile from the attacking ship toward the target with predictive
     * aiming
     */
    private void fireProjectile(Ship ship, Ship target) {
        if (target == null) {
            return;
        }

        // Calculate predicted target position with some margin of error
        double[] predictedPosition = calculatePredictedTargetPosition(ship, target);

        // Create a projectile that aims at the predicted position instead of current
        // position
        Projectile projectile = new Projectile(
                ship.getOperator(),
                ship,
                target,
                ship.getX(),
                ship.getY(),
                GameConstants.getProjectileSpeed(),
                ship.getDamage(),
                GameConstants.getProjectileMaxRange(),
                predictedPosition[0],
                predictedPosition[1]);

        game.addProjectile(projectile);
    }

    /**
     * Calculates where the target ship will likely be when the projectile reaches
     * it
     */
    private double[] calculatePredictedTargetPosition(Ship shooter, Ship target) {
        // Get target's current position and velocity
        double targetX = target.getX();
        double targetY = target.getY();
        double targetSpeed = target.getSpeed();
        double targetDirection = target.getDirection();

        // Calculate distance from shooter to target
        double distanceToTarget = Math.hypot(targetX - shooter.getX(), targetY - shooter.getY());

        // Calculate time for projectile to reach target (approximate)
        double timeToReach = distanceToTarget / GameConstants.getProjectileSpeed();

        // Predict where target will be
        double predictedX = targetX + Math.cos(targetDirection) * targetSpeed * timeToReach;
        double predictedY = targetY + Math.sin(targetDirection) * targetSpeed * timeToReach;

        // Add some margin of error based on distance (further targets are harder to
        // predict)
        double errorMargin = Math.min(20, distanceToTarget * 0.1);
        double randomError = (Math.random() - 0.5) * 2 * errorMargin;
        double errorAngle = Math.random() * 2 * Math.PI;

        predictedX += Math.cos(errorAngle) * randomError;
        predictedY += Math.sin(errorAngle) * randomError;

        return new double[] { predictedX, predictedY };
    }

    /**
     * Checks if a ship is facing towards its target (within a reasonable angle)
     * Ships can only shoot in the direction they're facing
     */
    private boolean isShipFacingTarget(Ship ship, Ship target) {
        // Calculate angle from ship to target
        double dx = target.getX() - ship.getX();
        double dy = target.getY() - ship.getY();
        double angleToTarget = Math.atan2(dy, dx);

        // Get ship's current facing direction
        double shipDirection = ship.getDirection();

        // Calculate angle difference
        double angleDifference = Math.abs(angleToTarget - shipDirection);

        // Normalize angle difference to [0, PI]
        if (angleDifference > Math.PI) {
            angleDifference = 2 * Math.PI - angleDifference;
        }

        // Ship can shoot if facing within 45 degrees (PI/4 radians) of target
        final double SHOOTING_ANGLE_TOLERANCE = Math.PI / 4; // 45 degrees
        return angleDifference <= SHOOTING_ANGLE_TOLERANCE;
    }

    /**
     * Removes a ship from combat management when it's destroyed or removed
     */
    public void removeShip(Ship ship) {
        combatStates.remove(ship);

        // Also remove this ship as a target from other ships and exit their combat
        for (Map.Entry<Ship, CombatState> entry : combatStates.entrySet()) {
            CombatState state = entry.getValue();
            if (state.combatTarget == ship) {
                Ship targetingShip = entry.getKey();
                exitCombat(targetingShip, state);
            }
        }
    }

    /**
     * Checks if a ship is currently in combat
     */
    public boolean isInCombat(Ship ship) {
        CombatState state = combatStates.get(ship);
        return state != null && state.inCombat;
    }

    /**
     * Gets the combat target of a ship
     */
    public Ship getCombatTarget(Ship ship) {
        CombatState state = combatStates.get(ship);
        return state != null ? state.combatTarget : null;
    }

    /**
     * Checks if a ship is stationary due to combat
     */
    public boolean isStationary(Ship ship) {
        CombatState state = combatStates.get(ship);
        return state != null && state.inCombat;
    }
}