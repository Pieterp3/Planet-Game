package home.game.planets;

import java.util.ArrayList;
import java.util.List;

import home.game.Game;
import home.game.GameConstants;
import home.game.Ship;
import home.game.abilities.AbilityType;
import home.game.challenges.ChallengeManager;
import home.game.operators.Bot;

import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.operators.player.PlayerData;
import home.sounds.Sound;
import home.game.operators.player.UpgradeType;

public class Planet {

    private Operator operator;
    private Game game; // Reference to game for ability checks
    private double x;
    private double y;
    private int health;
    private PlanetType planetType;
    private long lastShipTime;
    private long shipInterval = (long) (1000 / GameConstants.getDefaultShipsPerSecond());
    private List<Planet> targets;
    private int maxTargets = 1;
    private int targetIndex = 0;
    private int maxHealth = GameConstants.getMaxPlanetHealth();
    private int healthRegenRate = GameConstants.getPlanetHealthRegenRate(); // Health per second
    private long lastRegenTime = System.currentTimeMillis();
    private List<Ship> stationedShips;
    private double rotationAngle = 0; // For visual rotation effect
    private PlanetFeatures features; // Visual features like craters, rings, moons

    // Orbital mechanics properties
    private double orbitCenterX = GameConstants.getGameWidth() / 2.0; // Center of screen X
    private double orbitCenterY = GameConstants.getGameHeight() / 2.0; // Center of screen Y
    private double semiMajorAxis; // Semi-major axis of ellipse (horizontal for horizontal orbits)
    private double semiMinorAxis; // Semi-minor axis of ellipse (vertical for horizontal orbits)
    private double orbitalAngle = 0; // Current angle in orbit (0 to 2π)
    private double orbitalSpeed; // Radians per tick (positive = clockwise, negative = counter-clockwise)
    private boolean isVerticalOrbit; // true for vertical ellipse, false for horizontal ellipse
    private double zIndex; // Depth for rendering order (higher values render on top)
    private double depthScale = 1.0; // Size scaling based on distance from viewer

    public Planet(Operator operator, double x, double y, int health, PlanetType planetType) {
        this.operator = operator;
        this.x = x;
        this.y = y;
        this.health = health;
        this.planetType = planetType;
        this.lastShipTime = System.currentTimeMillis();
        this.stationedShips = new ArrayList<>();
        this.targets = new ArrayList<>();
        this.features = new PlanetFeatures(x, y, planetType);
        setMaxTargets();
    }

    // Constructor with orbital parameters
    public Planet(Operator operator, int health, PlanetType planetType, double semiMajorAxis, double semiMinorAxis,
            double initialAngle, double orbitalSpeed, boolean isVerticalOrbit, double zIndex) {
        this.operator = operator;
        this.health = health;
        this.planetType = planetType;
        this.lastShipTime = System.currentTimeMillis();
        this.stationedShips = new ArrayList<>();
        this.targets = new ArrayList<>();

        // Set orbital parameters
        this.semiMajorAxis = semiMajorAxis;
        this.semiMinorAxis = semiMinorAxis;
        this.orbitalAngle = initialAngle;
        this.orbitalSpeed = orbitalSpeed;
        this.isVerticalOrbit = isVerticalOrbit;
        this.zIndex = zIndex;

        // Calculate depth scale based on z-index (closer planets appear larger)
        // zIndex 0 = normal size, positive = closer (larger), negative = farther
        // (smaller)
        this.depthScale = 1.0 + (zIndex * 0.3); // 30% size change per z-index unit

        // Calculate initial position
        updatePosition();

        this.features = new PlanetFeatures((int) x, (int) y, planetType);
        setMaxTargets();
    }

    private void setMaxTargets() {
        maxTargets = health / (getMaxHealth() / 4);
        maxTargets += planetType.getExtraTargets();
        maxTargets = Math.max(1, maxTargets);
    }

    /**
     * Gets the adjusted ship spawn interval, taking into account upgrades and
     * planet type
     */
    private long getAdjustedShipInterval() {
        double baseInterval = shipInterval;

        // Apply player upgrades for ship spawn speed
        if (operator instanceof Player) {
            PlayerData playerData = PlayerData.getInstance();
            double spawnSpeedMultiplier = playerData.getUpgradeMultiplier(UpgradeType.SHIP_SPAWN_SPEED);
            // Higher multiplier means faster spawn (lower interval)
            baseInterval = baseInterval / spawnSpeedMultiplier;

            // Apply Factory Hype ability bonus if active
            if (game != null) {
                double abilityMultiplier = game.getAbilityManager().getShipSpawnSpeedMultiplier();
                baseInterval = baseInterval / abilityMultiplier;
            }
        } else if (operator instanceof Bot) {
            // Apply bot upgrades for ship spawn speed
            Bot bot = (Bot) operator;
            double spawnSpeedMultiplier = bot.getBotUpgradeMultiplier(UpgradeType.SHIP_SPAWN_SPEED);
            // Higher multiplier means faster spawn (lower interval)
            baseInterval = baseInterval / spawnSpeedMultiplier;
        }
        baseInterval = (long) (baseInterval / planetType.getShipProductionMultiplier());
        return Math.max(100, (long) baseInterval); // Minimum 100ms interval
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Operator getOperator() {
        return operator;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public PlanetType getType() {
        return planetType;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public void takeDamage(Ship ship) {
        int damage = ship.getDamage();
        if (ship.getOperator() == this.operator && damage > 0) {
            damage = -damage; // Heal if same operator
        } else {
            // Check shield ability for player planets
            if (this.operator instanceof Player && game != null) {
                if (game.getAbilityManager().isShieldActive() || GameConstants.arePlayerPlanetsInvincible()) {
                    if (damage > 0) {
                        return; // No damage when shield is active
                    }
                }
            }

            // Check shield ability for bot planets
            if (this.operator instanceof Bot) {
                Bot bot = (Bot) this.operator;
                if (bot.isBotShieldActive()) {
                    if (damage > 0) {
                        damage = (int) (damage * 0.5); // Reduce damage by 50% when bot shield is active
                    }
                }
            }

            // Apply planet damage reduction for player-owned planets
            if (this.operator instanceof Player) {
                PlayerData playerData = PlayerData.getInstance();
                double damageReduction = playerData.getUpgradePercentage(UpgradeType.PLANET_DAMAGE_REDUCTION);
                if (damage > 0) {
                    damage = (int) (damage * (1.0 - damageReduction / 100.0));
                }
            }

            // Apply defence planet type bonus
            damage = (int) (damage / planetType.getDefenceMultiplier());

            if (this.health < 0) {
                this.health = Math.abs(health);
                Operator previousOperator = this.operator;
                this.operator = ship.getOperator();
                this.targets.clear(); // Clear targets on takeover
                this.stationedShips.clear(); // Clear stationed ships on takeover
                if (this.operator instanceof Player) {
                    game.getAbilityManager().removeInfection(this);
                }
                game.getAbilityManager().removeCurse(this);

                // Track challenge progress if game is available
                if (game != null) {
                    ChallengeManager challengeManager = ChallengeManager.getInstance();

                    // Track planet capture if player captured it
                    if (ship.getOperator() instanceof Player) {
                        challengeManager.onPlanetCaptured(this.planetType);
                    }

                    // Track planet loss if player lost it
                    if (previousOperator instanceof Player) {
                        challengeManager.onPlanetLost();
                    }
                }
            }
        }
        int effectiveMaxHealth = getMaxHealth();
        if (this.health == effectiveMaxHealth && damage < 0) {
            // Station the ship if planet is at full health
            stationedShips.add(ship);
            ship.getOperator().removeShip(ship);
            setMaxTargets();
            return;
        }

        // Play planet damage sound if damage > 0 and game is available
        if (damage > 0 && game != null) {
            game.getSoundManager().play(Sound.PLANET_DAMAGE);
        }

        this.health -= damage;
        setMaxTargets();
    }

    public boolean isShipStationed(Ship ship) {
        return stationedShips.contains(ship);
    }

    public void tick() {
        long currentTime = System.currentTimeMillis();
        if (stationedShips.size() > 0 && targets.size() > 0) {
            Ship ship = stationedShips.remove(0);
            ship.resetCreationTime();
            ship.setTarget(chooseTarget());
            ship.setLocation(x, y);
            operator.addShip(ship);
            for (int i = 0; i < 2; i++) {
                ship.move();
            }
        }
        // Check if this planet should be affected by freeze ability
        boolean canSpawnShip = true;
        if (game != null && !(operator instanceof Player)) {
            // Only check freeze for bot planets
            canSpawnShip = !game.getAbilityManager().isFreezeActive();
        }

        if (currentTime - lastShipTime >= getAdjustedShipInterval() && operator != null && targets.size() > 0
                && canSpawnShip) {
            // Produce a ship
            double shipSpeed = GameConstants.getDefaultShipSpeed();
            int shipHealth = GameConstants.getDefaultShipHealth();
            int shipDamage = GameConstants.getDefaultShipDamage();

            // Apply planet type bonuses
            shipSpeed *= planetType.getShipSpeedMultiplier();
            shipHealth = (int) (shipHealth * planetType.getDefenceMultiplier());// ships dont have defence, so use
                                                                                // planets defence multiplier
            shipDamage = (int) (shipDamage * planetType.getAttackMultiplier());

            // Check if this planet is infected - infected planets produce ships for the
            // player
            boolean isInfected = game != null && game.getAbilityManager().isPlanetInfected(this);
            Operator shipOperator = isInfected ? game.getPlayer() : operator;

            // Apply player upgrades if this is a player-owned planet OR if infected
            if (operator instanceof Player || isInfected) {
                PlayerData playerData = PlayerData.getInstance();

                // Apply ship upgrades
                shipSpeed *= playerData.getUpgradeMultiplier(UpgradeType.SHIP_SPEED);
                shipHealth = (int) (shipHealth * playerData.getUpgradeMultiplier(UpgradeType.SHIP_HEALTH));
                shipDamage = (int) (shipDamage * playerData.getUpgradeMultiplier(UpgradeType.SHIP_DAMAGE));

                // Apply ability bonuses if game reference is available
                if (game != null) {
                    shipSpeed *= game.getAbilityManager().getShipSpeedMultiplier();
                    shipHealth = (int) (shipHealth * game.getAbilityManager().getShipHealthMultiplier());
                    shipDamage = (int) (shipDamage * game.getAbilityManager().getShipDamageMultiplier());
                }

                // Handle double ship chance
                double doubleShipChance = playerData.getUpgradePercentage(UpgradeType.DOUBLE_SHIP_CHANCE);
                boolean createDoubleShip = Math.random() < (doubleShipChance / 100.0);

                Planet targetPlanet = isInfected ? chooseTargetForOperator(shipOperator) : chooseTarget();
                Ship newShip = new Ship(shipOperator, this, targetPlanet, shipSpeed, shipHealth, shipDamage);
                newShip.setLocation(x, y);
                stationedShips.add(newShip);

                // Create second ship if double ship chance triggers
                if (createDoubleShip) {
                    Planet secondTargetPlanet = isInfected ? chooseTargetForOperator(shipOperator) : chooseTarget();
                    Ship secondShip = new Ship(shipOperator, this, secondTargetPlanet, shipSpeed, shipHealth,
                            shipDamage);
                    shipOperator.addShip(secondShip);
                }
            } else {
                // Regular ship creation for bots
                // Apply bot upgrades if operator is a bot
                if (shipOperator instanceof Bot) {
                    Bot bot = (Bot) shipOperator;
                    shipSpeed *= bot.getBotUpgradeMultiplier(UpgradeType.SHIP_SPEED);
                    shipHealth = (int) (shipHealth * bot.getBotUpgradeMultiplier(UpgradeType.SHIP_HEALTH));
                    shipDamage = (int) (shipDamage * bot.getBotUpgradeMultiplier(UpgradeType.SHIP_DAMAGE));
                }
                // Apply curse effects if this planet is cursed
                if (game != null && game.getAbilityManager().isPlanetCursed(this)) {
                    int curseReduction = PlayerData.getInstance().getAbilityPower(AbilityType.CURSE);
                    double curseMultiplier = 1.0 - (curseReduction / 100.0);
                    shipSpeed *= curseMultiplier;
                    shipHealth = (int) (shipHealth * curseMultiplier);
                    shipDamage = (int) (shipDamage * curseMultiplier);
                }
                Planet targetPlanet = isInfected ? chooseTargetForOperator(shipOperator) : chooseTarget();
                Ship newShip = new Ship(shipOperator, this, targetPlanet, shipSpeed, shipHealth, shipDamage);
                shipOperator.addShip(newShip);
            }

            lastShipTime = currentTime;
        }
        // Regenerate health
        if (currentTime - lastRegenTime >= 1000) {
            double healthToRegen = healthRegenRate * planetType.getHealthRegenMultiplier();
            healthToRegen = Math.min(healthToRegen, getMaxHealth() - health);
            this.health += healthToRegen;
            lastRegenTime = currentTime;
        }

        // Update visual features (moons orbiting)
        features.updateMoons();

        // Update orbital position
        updateOrbitalPosition();
    }

    private Planet chooseTarget() {
        // Cycle through targets
        if (targetIndex >= targets.size()) {
            targetIndex = 0;
        }
        return targets.get(targetIndex++);
    }

    /**
     * Chooses a target for a specific operator (used for infected planets)
     */
    private Planet chooseTargetForOperator(Operator shipOperator) {
        if (game == null) {
            return chooseTarget(); // Fallback to regular targeting
        }

        // Find enemy planets for the ship operator
        List<Planet> enemyPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet != this && planet.getOperator() != shipOperator) {
                enemyPlanets.add(planet);
            }
        }

        if (enemyPlanets.isEmpty()) {
            return null; // No valid targets
        }

        // Choose a random enemy planet
        int randomIndex = (int) (Math.random() * enemyPlanets.size());
        return enemyPlanets.get(randomIndex);
    }

    private void addTarget(Planet planet) {
        if (targets.size() < maxTargets && !targets.contains(planet) && planet != this) {
            targets.add(planet);
        }
    }

    public void removeTarget(Planet planet) {
        targets.remove(planet);
    }

    public int getHealth() {
        return health;
    }

    public List<Planet> getTargets() {
        return targets;
    }

    public int getMaxTargets() {
        return maxTargets;
    }

    public int getMaxHealth() {
        // Apply planet health upgrades for player-owned planets
        if (operator instanceof Player) {
            PlayerData playerData = PlayerData.getInstance();
            double healthMultiplier = playerData.getUpgradeMultiplier(UpgradeType.PLANET_HEALTH);
            return (int) (maxHealth * healthMultiplier);
        }
        return maxHealth;
    }

    public double getRotationAngle() {
        rotationAngle += features.getRotationSpeed();
        if (rotationAngle >= 2 * Math.PI) {
            rotationAngle -= 2 * Math.PI;
        }
        return rotationAngle;
    }

    public PlanetFeatures getFeatures() {
        return features;
    }

    /**
     * Updates the planet's position based on its orbital mechanics
     */
    private void updatePosition() {
        if (semiMajorAxis > 0 && semiMinorAxis > 0) {
            // Calculate elliptical orbit position
            double cosAngle = Math.cos(orbitalAngle);
            double sinAngle = Math.sin(orbitalAngle);

            if (isVerticalOrbit) {
                // Vertical ellipse: semi-major axis is vertical
                x = (int) (orbitCenterX + semiMinorAxis * cosAngle);
                y = (int) (orbitCenterY + semiMajorAxis * sinAngle);
            } else {
                // Horizontal ellipse: semi-major axis is horizontal
                x = (int) (orbitCenterX + semiMajorAxis * cosAngle);
                y = (int) (orbitCenterY + semiMinorAxis * sinAngle);
            }
        } else {
            // Fallback to center if orbital parameters are invalid
            x = (int) orbitCenterX;
            y = (int) orbitCenterY;
        }
    }

    /**
     * Updates the planet's orbital position for each game tick
     */
    private void updateOrbitalPosition() {
        if (orbitalSpeed != 0) {
            // Check if this planet is orbitally frozen
            boolean isOrbitallyFrozen = false;
            if (game != null && game.getAbilityManager() != null) {
                isOrbitallyFrozen = game.getAbilityManager().isPlanetOrbitallyFrozen(this);
            }

            // Only update orbital position if not frozen
            if (!isOrbitallyFrozen) {
                orbitalAngle += orbitalSpeed;

                // Normalize angle to keep it in 0 to 2π range
                while (orbitalAngle >= 2 * Math.PI) {
                    orbitalAngle -= 2 * Math.PI;
                }
                while (orbitalAngle < 0) {
                    orbitalAngle += 2 * Math.PI;
                }

                updatePosition();
            }
        }
    }

    /**
     * Sets orbital parameters for this planet
     */
    public void setOrbitalParameters(double semiMajorAxis, double semiMinorAxis, double initialAngle,
            double orbitalSpeed, boolean isVerticalOrbit) {
        this.semiMajorAxis = semiMajorAxis;
        this.semiMinorAxis = semiMinorAxis;
        this.orbitalAngle = initialAngle;
        this.orbitalSpeed = orbitalSpeed;
        this.isVerticalOrbit = isVerticalOrbit;
        updatePosition();
    }

    /**
     * Gets the z-index for rendering order
     */
    public double getZIndex() {
        return zIndex;
    }

    /**
     * Gets the depth scale for size rendering
     */
    public double getDepthScale() {
        return depthScale;
    }

    /**
     * Gets the actual rendered radius of the planet (considering depth scaling)
     */
    public double getActualRadius() {
        return (GameConstants.getPlanetSize() * depthScale) / 2.0;
    }

    /**
     * Gets the orbital speed for debugging
     */
    public double getOrbitalSpeed() {
        return orbitalSpeed;
    }

    /**
     * Gets the current orbital angle for debugging
     */
    public double getOrbitalAngle() {
        return orbitalAngle;
    }

    /**
     * Gets the semi-major axis of the planet's orbit
     */
    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    /**
     * Gets the semi-minor axis of the planet's orbit
     */
    public double getSemiMinorAxis() {
        return semiMinorAxis;
    }

    /**
     * Gets whether the planet has a vertical orbit
     */
    public boolean isVerticalOrbit() {
        return isVerticalOrbit;
    }

    /**
     * Gets the orbit center X coordinate
     */
    public double getOrbitCenterX() {
        return orbitCenterX;
    }

    /**
     * Gets the orbit center Y coordinate
     */
    public double getOrbitCenterY() {
        return orbitCenterY;
    }

    public void attemptTargeting(Planet targetPlanet) {
        // If connection already exists, remove it
        if (getTargets().contains(targetPlanet)) {
            removeTarget(targetPlanet);
        } else {
            boolean isSameOperator = getOperator() == targetPlanet.getOperator();
            if (isSameOperator) {
                // If same operator, swap connection direction if reverse exists
                if (targetPlanet.getTargets().contains(this)) {
                    targetPlanet.removeTarget(this);
                    addTarget(targetPlanet);
                } else {
                    // No existing connection, add new one
                    addTarget(targetPlanet);
                }
            } else {
                // Different operators, just add one-way connection
                addTarget(targetPlanet);
            }
        }
    }

    /**
     * Forces immediate ship creation bypassing normal timing constraints
     */
    public void forceShipCreation() {
        if (operator == null || targets.isEmpty()) {
            return; // Can't create ships without owner or targets
        }

        // Set last ship time to allow immediate creation on next tick
        lastShipTime = System.currentTimeMillis() - getAdjustedShipInterval() - 100;
    }

    /**
     * Heals the planet by the specified amount
     */
    public void heal(int healAmount) {
        this.health = Math.min(this.health + healAmount, maxHealth);
    }

    /**
     * Checks if this planet is currently orbitally frozen
     */
    public boolean isOrbitallyFrozen() {
        if (game != null && game.getAbilityManager() != null) {
            return game.getAbilityManager().isPlanetOrbitallyFrozen(this);
        }
        return false;
    }

}
