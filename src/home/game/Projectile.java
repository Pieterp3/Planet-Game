package home.game;

import home.game.operators.Operator;

public class Projectile {

    private Operator operator;
    private Ship sourceShip;
    private Ship targetShip;
    private double x;
    private double y;
    private double speed;
    private int damage;
    private double direction;
    private boolean active;
    private double maxRange;
    private double distanceTraveled;

    public Projectile(Operator operator, Ship sourceShip, Ship targetShip, double x, double y, double speed, int damage,
            double maxRange) {
        this.operator = operator;
        this.sourceShip = sourceShip;
        this.targetShip = targetShip;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;
        this.maxRange = maxRange;
        this.distanceTraveled = 0;
        this.active = true;

        // Calculate initial direction toward target
        if (targetShip != null) {
            double dx = targetShip.getX() - x;
            double dy = targetShip.getY() - y;
            this.direction = Math.atan2(dy, dx);
        }
    }

    /**
     * Constructor for predictive targeting - aims at specific coordinates
     */
    public Projectile(Operator operator, Ship sourceShip, Ship targetShip, double x, double y, double speed, int damage,
            double maxRange, double targetX, double targetY) {
        this.operator = operator;
        this.sourceShip = sourceShip;
        this.targetShip = targetShip;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;
        this.maxRange = maxRange;
        this.distanceTraveled = 0;
        this.active = true;

        // Calculate initial direction toward predicted target position
        double dx = targetX - x;
        double dy = targetY - y;
        this.direction = Math.atan2(dy, dx);
    }

    public void tick() {
        if (!active)
            return;

        // Move projectile
        double deltaX = speed * Math.cos(direction);
        double deltaY = speed * Math.sin(direction);

        x += deltaX;
        y += deltaY;
        distanceTraveled += Math.hypot(deltaX, deltaY);

        // Deactivate if traveled too far
        if (distanceTraveled > maxRange) {
            active = false;
            return;
        }

        // Check if projectile is out of bounds
        if (x < 0 || x > GameConstants.getGameWidth() || y < 0 || y > GameConstants.getGameHeight()) {
            active = false;
            return;
        }
    }

    /**
     * Checks if this projectile hits the given ship
     * 
     * @param ship The ship to check collision with
     * @return true if collision occurred
     */
    public boolean checkCollision(Ship ship) {
        if (!active || ship == sourceShip)
            return false;

        // Only hit enemy ships
        if (ship.getOperator() == operator)
            return false;

        double distance = Math.hypot(ship.getX() - x, ship.getY() - y);
        return distance <= GameConstants.getShipSize() / 2;
    }

    /**
     * Applies damage to the target ship and deactivates this projectile
     * 
     * @param ship The ship to damage
     */
    public void hitShip(Ship ship) {
        if (active && ship.getOperator() != operator) {
            ship.takeDamage(damage);
            active = false;
        }
    }

    // Getters
    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public int getX() {
        return (int) Math.round(x);
    }

    public int getY() {
        return (int) Math.round(y);
    }

    public double getDoubleX() {
        return x;
    }

    public double getDoubleY() {
        return y;
    }

    public double getDirection() {
        return direction;
    }

    public Operator getOperator() {
        return operator;
    }

    public Ship getSourceShip() {
        return sourceShip;
    }

    public Ship getTargetShip() {
        return targetShip;
    }

    public int getDamage() {
        return damage;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public double getMaxRange() {
        return maxRange;
    }
}