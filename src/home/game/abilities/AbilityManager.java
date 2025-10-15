package home.game.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import home.game.Game;
import home.game.GameConstants;
import home.game.Ship;
import home.game.challenges.ChallengeManager;
import home.game.operators.Bot;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.operators.player.PlayerData;
import home.game.operators.player.UpgradeType;
import home.game.planets.Planet;

public class AbilityManager {
    private Game game;
    private PlayerData playerData;
    private Map<AbilityType, Long> cooldowns;
    private Map<AbilityType, Long> activeEffects;
    private Random random;

    // Ability effect tracking (player only - for backward compatibility)
    private boolean freezeActive = false;
    private boolean shieldActive = false;
    private boolean factoryHypeActive = false;
    private boolean improvedFactoriesActive = false;

    // New ability tracking (player only - for backward compatibility)
    private Map<Planet, Long> cursedPlanets = new HashMap<>(); // Planet -> curse end time
    private List<BlackHole> blackHoles = new ArrayList<>();
    private boolean planetaryFlameActive = false;
    private Map<Planet, Long> infectedPlanets = new HashMap<>(); // Planet -> infection end time
    private boolean unstoppableShipsActive = false;
    private Map<Planet, Long> orbitalFrozenPlanets = new HashMap<>(); // Planet -> freeze end time

    // Multi-operator ability tracking
    private Map<Operator, Boolean> operatorFreezeActive = new HashMap<>();
    private Map<Operator, Boolean> operatorShieldActive = new HashMap<>();
    private Map<Operator, Boolean> operatorFactoryHypeActive = new HashMap<>();
    private Map<Operator, Boolean> operatorImprovedFactoriesActive = new HashMap<>();
    private Map<Operator, Map<Planet, Long>> operatorCursedPlanets = new HashMap<>();
    private Map<Operator, List<BlackHole>> operatorBlackHoles = new HashMap<>();
    private Map<Operator, Boolean> operatorPlanetaryFlameActive = new HashMap<>();
    private Map<Operator, Map<Planet, Long>> operatorInfectedPlanets = new HashMap<>();
    private Map<Operator, Boolean> operatorUnstoppableShipsActive = new HashMap<>();
    private Map<Operator, Map<Planet, Long>> operatorOrbitalFrozenPlanets = new HashMap<>();

    // Operator effect expiry times
    private Map<Operator, Long> operatorFreezeExpiry = new HashMap<>();
    private Map<Operator, Long> operatorShieldExpiry = new HashMap<>();
    private Map<Operator, Long> operatorFactoryHypeExpiry = new HashMap<>();
    private Map<Operator, Long> operatorImprovedFactoriesExpiry = new HashMap<>();
    private Map<Operator, Long> operatorPlanetaryFlameExpiry = new HashMap<>();
    private Map<Operator, Long> operatorUnstoppableShipsExpiry = new HashMap<>();
    private Map<Operator, Long> operatorOrbitalFreezeExpiry = new HashMap<>();

    public AbilityManager(Game game) {
        this.game = game;
        this.playerData = PlayerData.getInstance();
        this.cooldowns = new HashMap<>();
        this.activeEffects = new HashMap<>();
        this.random = new Random();

        // Initialize cooldowns
        for (AbilityType type : AbilityType.values()) {
            cooldowns.put(type, 0L);
            activeEffects.put(type, 0L);
        }
    }

    public boolean canUseAbility(AbilityType type) {
        if (!playerData.isAbilityUnlocked(type))
            return false;
        return System.currentTimeMillis() >= cooldowns.get(type);
    }

    public long getRemainingCooldown(AbilityType type) {
        long current = System.currentTimeMillis();
        long cooldownEnd = cooldowns.get(type);
        return Math.max(0, cooldownEnd - current);
    }

    public boolean isAbilityActive(AbilityType type) {
        return System.currentTimeMillis() < activeEffects.get(type);
    }

    public long getRemainingDuration(AbilityType type) {
        long current = System.currentTimeMillis();
        long effectEnd = activeEffects.get(type);
        return Math.max(0, effectEnd - current);
    }

    public boolean activateAbility(AbilityType type) {
        if (!canUseAbility(type))
            return false;

        // Track ability usage for challenges
        ChallengeManager challengeManager = ChallengeManager.getInstance();
        challengeManager.onAbilityUsed(type);

        long currentTime = System.currentTimeMillis();
        double duration = playerData.getAbilityDuration(type);
        int power = playerData.getAbilityPower(type);

        // Set cooldown (base cooldown + duration)
        long cooldownDuration = GameConstants.getBaseAbilityCooldown() + (long) (duration * 1000);
        cooldownDuration = (long) (cooldownDuration
                * (1.0 - playerData.getUpgradeMultiplier(UpgradeType.ABILITY_COOLDOWN)
                        / GameConstants.getAbilityCooldownReductionPercent()));
        if (GameConstants.removeAbilityCooldowns()) {
            cooldownDuration = 0; // No cooldown for testing
        }
        cooldowns.put(type, currentTime + cooldownDuration);

        // Activate ability based on type
        switch (type) {
            case FREEZE:
                activateFreeze(duration);
                break;
            case MISSILE_BARRAGE:
                activateMissileBarrage(power);
                break;
            case SHIELD:
                activateShield(duration);
                break;
            case FACTORY_HYPE:
                activateFactoryHype(duration);
                break;
            case IMPROVED_FACTORIES:
                activateImprovedFactories(duration);
                break;
            case ANSWERED_PRAYERS:
                activateAnsweredPrayers(power);
                break;
            case CURSE:
                activateCurse(duration, power);
                break;
            case BLACK_HOLE:
                activateBlackHole(duration, power);
                break;
            case PLANETARY_FLAME:
                activatePlanetaryFlame(duration, power);
                break;
            case PLANETARY_INFECTION:
                activatePlanetaryInfection(duration, power);
                break;
            case UNSTOPPABLE_SHIPS:
                activateUnstoppableShips(duration, power);
                break;
            case ORBITAL_FREEZE:
                activateOrbitalFreeze(duration, power);
                break;
        }

        return true;
    }

    private void activateFreeze(double duration) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        activeEffects.put(AbilityType.FREEZE, endTime);
        freezeActive = true;
    }

    public void removeInfection(Planet planet) {
        infectedPlanets.remove(planet);
    }

    public void removeCurse(Planet planet) {
        cursedPlanets.remove(planet);
    }

    private void activateMissileBarrage(int missileCount) {
        List<Planet> enemyPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() instanceof Bot) {
                enemyPlanets.add(planet);
            }
        }

        if (enemyPlanets.isEmpty())
            return;

        for (int i = 0; i < missileCount; i++) {
            Planet target = enemyPlanets.get(random.nextInt(enemyPlanets.size()));
            // Create a missile ship that will attack the planet
            int missileDamage = (int) (GameConstants.getDefaultShipDamage() *
                    playerData.getUpgradeMultiplier(UpgradeType.SHIP_DAMAGE)
                    * GameConstants.getMissileDamageMultiplier());

            Ship missile = new Ship(game.getPlayer(), null, target,
                    GameConstants.getProjectileSpeed() * GameConstants.getMissileSpeedMultiplier(), 1, missileDamage,
                    true); // Mark as missile
            missile.setLocation(GameConstants.getGameWidth() / 2, GameConstants.getGameHeight() / 2);
            game.addShip(missile);
            game.getPlayer().addShip(missile);
        }
    }

    private void activateShield(double duration) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        activeEffects.put(AbilityType.SHIELD, endTime);
        shieldActive = true;
    }

    private void activateFactoryHype(double duration) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        activeEffects.put(AbilityType.FACTORY_HYPE, endTime);
        factoryHypeActive = true;
    }

    private void activateImprovedFactories(double duration) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        activeEffects.put(AbilityType.IMPROVED_FACTORIES, endTime);
        improvedFactoriesActive = true;
    }

    private long lastHealingTime = 0;

    private void activateAnsweredPrayers(int healingPercent) {
        int actualHealing = Math.min(100, healingPercent);
        lastHealingTime = System.currentTimeMillis(); // Track when healing happened

        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() instanceof Player) {
                int maxHealth = planet.getMaxHealth();
                int healAmount = (int) (maxHealth * (actualHealing / 100.0));
                healAmount = Math.min(healAmount, maxHealth - planet.getHealth());
                if (healAmount <= 0)
                    continue;
                // Use takeDamage with negative value to heal
                Ship healingShip = new Ship(game.getPlayer(), planet, planet, 0, 1, healAmount);
                planet.takeDamage(healingShip);
            }
        }
    }

    public boolean wasHealingJustUsed() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastHealingTime) < 100; // Within 100ms of healing activation
    }

    private void activateCurse(double duration, int statReduction) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);

        // Curse all enemy planets
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() instanceof Bot) {
                cursedPlanets.put(planet, endTime);
            }
        }
    }

    private void activateBlackHole(double duration, int eventHorizon) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);

        // Cap event horizon size (cap now configurable via
        // GameConstants.getBlackHoleBasePower()*0.5 + ...)
        int cappedHorizon = Math.min(150, eventHorizon);

        // Find a planet owned by the player to spawn the black hole in orbit
        List<Planet> playerPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() == game.getPlayer()) {
                playerPlanets.add(planet);
            }
        }

        double blackHoleX, blackHoleY;

        if (!playerPlanets.isEmpty()) {
            // Choose a random planet owned by the player
            Planet orbitPlanet = playerPlanets.get(random.nextInt(playerPlanets.size()));

            // Calculate orbit distance (planet radius + black hole radius + some spacing)
            double orbitDistance = orbitPlanet.getActualRadius() + cappedHorizon / 2.0 + 30;

            // Generate random angle for orbit position
            double angle = random.nextDouble() * 2 * Math.PI;

            // Calculate black hole position in orbit
            blackHoleX = orbitPlanet.getX() + Math.cos(angle) * orbitDistance;
            blackHoleY = orbitPlanet.getY() + Math.sin(angle) * orbitDistance;
        } else {
            // Fallback: if no player planets exist, spawn at random location
            int margin = cappedHorizon / 2 + 50;
            blackHoleX = margin + random.nextDouble() * (GameConstants.getGameWidth() - 2 * margin);
            blackHoleY = margin + random.nextDouble() * (GameConstants.getGameHeight() - 2 * margin);
        }

        blackHoles.add(new BlackHole(blackHoleX, blackHoleY, cappedHorizon, endTime, game.getPlayer()));
    }

    private void activatePlanetaryFlame(double duration, int flamePower) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        activeEffects.put(AbilityType.PLANETARY_FLAME, endTime);
        planetaryFlameActive = true;

        // The flame effects will be handled in the update method and rendering
    }

    // Update method to be called each game tick
    public void update() {
        long currentTime = System.currentTimeMillis();

        // Check if abilities have expired
        if (freezeActive && currentTime >= activeEffects.get(AbilityType.FREEZE)) {
            freezeActive = false;
        }
        if (shieldActive && currentTime >= activeEffects.get(AbilityType.SHIELD)) {
            shieldActive = false;
        }
        if (factoryHypeActive && currentTime >= activeEffects.get(AbilityType.FACTORY_HYPE)) {
            factoryHypeActive = false;
        }
        if (improvedFactoriesActive && currentTime >= activeEffects.get(AbilityType.IMPROVED_FACTORIES)) {
            improvedFactoriesActive = false;
        }
        if (planetaryFlameActive && currentTime >= activeEffects.get(AbilityType.PLANETARY_FLAME)) {
            planetaryFlameActive = false;
        }

        // Clean up expired curses
        cursedPlanets.entrySet().removeIf(entry -> currentTime >= entry.getValue());

        // Clean up expired black holes and handle their effects
        blackHoles.removeIf(blackHole -> {
            if (blackHole.isExpired()) {
                return true;
            }

            // Update black hole rotation
            blackHole.rotationAngle += 0.1;

            // Check for planets in event horizon and damage them
            for (Planet planet : game.getPlanets()) {
                if (planet.getOperator() instanceof Bot) {
                    double distance = Math.sqrt(Math.pow(planet.getX() - blackHole.x, 2) +
                            Math.pow(planet.getY() - blackHole.y, 2));

                    if (distance < blackHole.eventHorizon / 2.0) {
                        // Damage planet (create a damage source) - damage scaled by base power
                        int damage = Math.max(10, GameConstants.getBlackHoleBasePower() / 10);
                        Ship damageSource = new Ship(game.getPlayer(), null, planet, 0, 1, damage);
                        planet.takeDamage(damageSource);
                    }
                }
            }

            return false;
        });

        // Handle planetary flame effects
        if (planetaryFlameActive) {
            handlePlanetaryFlameEffects();
        }

        // Handle unstoppable ships expiration
        if (unstoppableShipsActive && currentTime >= activeEffects.get(AbilityType.UNSTOPPABLE_SHIPS)) {
            unstoppableShipsActive = false;
        }

        // Handle planetary infection spreading
        if (!infectedPlanets.isEmpty()) {
            handlePlanetaryInfectionSpread(currentTime);
        }

        // Update operator-specific abilities
        updateOperatorAbilities(currentTime);
    }

    private void handlePlanetaryFlameEffects() {
        List<Planet> playerPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() instanceof Player) {
                playerPlanets.add(planet);
            }
        }

        // Activate flames on half of player planets (minimum 1)
        int flameCount = Math.max(1, playerPlanets.size() / 2);

        for (int i = 0; i < Math.min(flameCount, playerPlanets.size()); i++) {
            Planet flamePlanet = playerPlanets.get(i);
            double flameLength = playerData.getAbilityPower(AbilityType.PLANETARY_FLAME);

            // Check for enemies in flame range (rotating around planet)
            double rotationAngle = (System.currentTimeMillis() * 0.002) % (2 * Math.PI);

            for (int tower = 0; tower < 2; tower++) { // Two flame towers per planet
                double towerAngle = rotationAngle + (tower * Math.PI);
                double flameX = flamePlanet.getX() + Math.cos(towerAngle) * flameLength;
                double flameY = flamePlanet.getY() + Math.sin(towerAngle) * flameLength;

                // Check for enemy planets and ships in flame area
                for (Planet target : game.getPlanets()) {
                    if (target.getOperator() instanceof Bot) {
                        double distance = Math.sqrt(Math.pow(target.getX() - flameX, 2) +
                                Math.pow(target.getY() - flameY, 2));

                        if (distance < 30) { // Flame radius
                            Ship flameAttack = new Ship(game.getPlayer(), flamePlanet, target, 0, 1,
                                    playerData.getAbilityPower(AbilityType.PLANETARY_FLAME));
                            target.takeDamage(flameAttack);
                        }
                    }
                }

                // Destroy enemy ships in flame area
                game.getShips().removeIf(ship -> {
                    if (ship.getOperator() instanceof Bot) {
                        double distance = Math.sqrt(Math.pow(ship.getX() - flameX, 2) +
                                Math.pow(ship.getY() - flameY, 2));
                        return distance < 25; // Flame ship destruction radius
                    }
                    return false;
                });
            }
        }
    }

    private void handlePlanetaryInfectionSpread(long currentTime) {
        long infectionDuration = (long) (playerData.getAbilityDuration(AbilityType.PLANETARY_INFECTION) * 1000);
        int spreadDamage = playerData.getAbilityPower(AbilityType.PLANETARY_INFECTION);

        // First, check for infection spreading (do this globally every 200ms)
        if (currentTime % 200 < 50) { // Check every 200ms for spreading
            // Make a copy of current infected planets to avoid modification during
            // iteration
            List<Planet> currentlyInfected = new ArrayList<>(infectedPlanets.keySet());

            for (Planet infectedPlanet : currentlyInfected) {
                // Try to spread to nearby enemy planets
                for (Planet nearbyPlanet : game.getPlanets()) {
                    if (nearbyPlanet.getOperator() != game.getPlayer() &&
                            !infectedPlanets.containsKey(nearbyPlanet)) {

                        double distance = Math.sqrt(Math.pow(nearbyPlanet.getX() - infectedPlanet.getX(), 2) +
                                Math.pow(nearbyPlanet.getY() - infectedPlanet.getY(), 2));

                        // Spread infection if planets are intersecting (overlapping)
                        // Two circles intersect when distance < sum of their radii
                        double intersectionDistance = infectedPlanet.getActualRadius() + nearbyPlanet.getActualRadius();
                        if (distance < intersectionDistance) {
                            infectedPlanets.put(nearbyPlanet, currentTime);
                        }
                    }
                }
            }
        }

        // Collect planets that need damage before modifying the map
        List<Planet> planetsToDamage = new ArrayList<>();

        // Remove expired infections and collect planets that need damage
        infectedPlanets.entrySet().removeIf(entry -> {
            Planet infectedPlanet = entry.getKey();
            long infectionStartTime = entry.getValue();

            // Check if infection has expired
            if (currentTime - infectionStartTime > infectionDuration) {
                return true;
            }

            // Mark planet for damage if it's time (roughly every second)
            if ((currentTime - infectionStartTime) % 1000 < 50) {
                planetsToDamage.add(infectedPlanet);
            }

            return false;
        });

        // Deal damage to infected planets after map modification is complete
        for (Planet planet : planetsToDamage) {
            Ship infectionDamage = new Ship(game.getPlayer(), null, planet, 0, 1, spreadDamage);
            planet.takeDamage(infectionDamage);
        }
    }

    // Getters for game logic to check ability states
    public boolean isFreezeActive() {
        return freezeActive;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public boolean isFactoryHypeActive() {
        return factoryHypeActive;
    }

    public boolean isImprovedFactoriesActive() {
        return improvedFactoriesActive;
    }

    public boolean isPlanetaryFlameActive() {
        return planetaryFlameActive;
    }

    public boolean isUnstoppableShipsActive() {
        return unstoppableShipsActive;
    }

    public boolean isPlanetInfected(Planet planet) {
        return infectedPlanets.containsKey(planet);
    }

    public Map<Planet, Long> getInfectedPlanets() {
        Map<Planet, Long> allInfectedPlanets = new HashMap<>(infectedPlanets);

        // Add all operator infected planets
        for (Map<Planet, Long> operatorInfected : operatorInfectedPlanets.values()) {
            allInfectedPlanets.putAll(operatorInfected);
        }

        return allInfectedPlanets;
    }

    public Map<Planet, Long> getCursedPlanets() {
        Map<Planet, Long> allCursedPlanets = new HashMap<>(cursedPlanets);

        // Add all operator cursed planets
        for (Map<Planet, Long> operatorCursed : operatorCursedPlanets.values()) {
            allCursedPlanets.putAll(operatorCursed);
        }

        return allCursedPlanets;
    }

    public List<BlackHole> getBlackHoles() {
        List<BlackHole> allBlackHoles = new ArrayList<>(blackHoles);
        for (List<BlackHole> operatorHoles : operatorBlackHoles.values()) {
            allBlackHoles.addAll(operatorHoles);
        }
        return allBlackHoles;
    }

    public boolean isPlanetCursed(Planet planet) {
        return cursedPlanets.containsKey(planet) &&
                System.currentTimeMillis() < cursedPlanets.get(planet);
    }

    // Multipliers for abilities
    public double getShipSpawnSpeedMultiplier() {
        if (isFactoryHypeActive()) {
            return 3.0; // 3x faster ship spawning
        }
        return 1.0;
    }

    public double getShipDamageMultiplier() {
        if (isImprovedFactoriesActive()) {
            return 2.0; // 2x damage
        }
        return 1.0;
    }

    public double getShipHealthMultiplier() {
        if (isImprovedFactoriesActive()) {
            return 2.0; // 2x health
        }
        return 1.0;
    }

    public double getShipSpeedMultiplier() {
        if (isImprovedFactoriesActive()) {
            return 2.0; // 2x speed
        }
        return 1.0;
    }

    private void activatePlanetaryInfection(double duration, int power) {

        // Start infection on one random enemy planet
        long currentTime = System.currentTimeMillis();
        List<Planet> enemyPlanets = new ArrayList<>();

        for (int i = 0; i < power; i++) {
            // Infect multiple planets based on power level
            enemyPlanets.clear();

            // Collect all enemy planets
            for (Planet planet : game.getPlanets()) {
                if (planet.getOperator() != game.getPlayer() && !infectedPlanets.containsKey(planet)
                        && planet.getOperator() != null) {
                    enemyPlanets.add(planet);
                }
            }

            // Infect one random enemy planet if any exist
            if (!enemyPlanets.isEmpty()) {
                Random random = new Random();
                Planet targetPlanet = enemyPlanets.get(random.nextInt(enemyPlanets.size()));
                infectedPlanets.put(targetPlanet, currentTime);
            }
        }
        // The infection will spread in the update method
    }

    private void activateUnstoppableShips(double duration, int power) {
        unstoppableShipsActive = true;

        // Set the end time
        long currentTime = System.currentTimeMillis();
        activeEffects.put(AbilityType.UNSTOPPABLE_SHIPS, currentTime + (long) (duration * 1000));
    }

    private void activateOrbitalFreeze(double duration, int power) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        activeEffects.put(AbilityType.ORBITAL_FREEZE, endTime);

        // Get all enemy planets and freeze the first 'power' number of them
        List<Planet> enemyPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() instanceof Bot) {
                enemyPlanets.add(planet);
            }
        }

        // Freeze up to 'power' planets
        int planetsToFreeze = Math.min(power, enemyPlanets.size());
        for (int i = 0; i < planetsToFreeze; i++) {
            Planet planet = enemyPlanets.get(i);
            orbitalFrozenPlanets.put(planet, endTime);
        }
    }

    private void updateOperatorAbilities(long currentTime) {
        // Clean up expired operator freeze effects
        operatorFreezeExpiry.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                operatorFreezeActive.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clean up expired operator shield effects
        operatorShieldExpiry.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                operatorShieldActive.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clean up expired operator factory hype effects
        operatorFactoryHypeExpiry.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                operatorFactoryHypeActive.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clean up expired operator improved factories effects
        operatorImprovedFactoriesExpiry.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                operatorImprovedFactoriesActive.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clean up expired operator planetary flame effects
        operatorPlanetaryFlameExpiry.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                operatorPlanetaryFlameActive.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clean up expired operator unstoppable ships effects
        operatorUnstoppableShipsExpiry.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                operatorUnstoppableShipsActive.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clean up expired operator orbital freeze effects
        operatorOrbitalFreezeExpiry.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                // Remove all frozen planets for this operator
                operatorOrbitalFrozenPlanets.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clean up expired orbital frozen planets
        for (Map<Planet, Long> frozenByOperator : operatorOrbitalFrozenPlanets.values()) {
            frozenByOperator.entrySet().removeIf(entry -> currentTime >= entry.getValue());
        }

        // Clean up expired player orbital frozen planets
        orbitalFrozenPlanets.entrySet().removeIf(entry -> currentTime >= entry.getValue());

        // Clean up expired operator curses
        for (Map<Planet, Long> cursedByOperator : operatorCursedPlanets.values()) {
            cursedByOperator.entrySet().removeIf(entry -> currentTime >= entry.getValue());
        }

        // Clean up expired operator black holes and handle their effects
        for (Map.Entry<Operator, List<BlackHole>> entry : operatorBlackHoles.entrySet()) {
            Operator operator = entry.getKey();
            List<BlackHole> operatorBlackHoles = entry.getValue();

            operatorBlackHoles.removeIf(blackHole -> {
                if (blackHole.isExpired()) {
                    return true;
                }

                // Update black hole rotation
                blackHole.rotationAngle += 0.1;

                // Check for planets in event horizon and damage them
                for (Planet planet : game.getPlanets()) {
                    if (planet.getOperator() != operator) {
                        double distance = Math.sqrt(Math.pow(planet.getX() - blackHole.x, 2) +
                                Math.pow(planet.getY() - blackHole.y, 2));

                        if (distance < blackHole.eventHorizon / 2.0) {
                            // Damage planet (create a damage source)
                            Ship damageSource = new Ship(operator, null, planet, 0, 1, 50);
                            planet.takeDamage(damageSource);
                        }
                    }
                }
                for (Ship ship : game.getShips()) {
                    if (ship.getOperator() != operator) {
                        double distance = Math.sqrt(Math.pow(ship.getX() - blackHole.x, 2) +
                                Math.pow(ship.getY() - blackHole.y, 2));
                        if (distance < blackHole.eventHorizon / 2.0) {
                            ship.takeDamage(50);
                        }
                    }
                }

                return false;
            });
        }

        // Handle operator planetary infection spreading
        for (Map.Entry<Operator, Map<Planet, Long>> entry : operatorInfectedPlanets.entrySet()) {
            Operator operator = entry.getKey();
            Map<Planet, Long> infectedByOperator = entry.getValue();

            if (!infectedByOperator.isEmpty()) {
                handleOperatorPlanetaryInfectionSpread(operator, infectedByOperator, currentTime);
            }
        }
    }

    private void handleOperatorPlanetaryInfectionSpread(Operator operator, Map<Planet, Long> infectedPlanets,
            long currentTime) {
        long infectionDuration = 10000; // 10 seconds default duration
        int spreadDamage = 50; // Default damage

        // Handle infection spreading and damage
        List<Planet> newInfections = new ArrayList<>();
        List<Planet> planetsToDamage = new ArrayList<>();

        infectedPlanets.entrySet().removeIf(entry -> {
            Planet infectedPlanet = entry.getKey();
            long infectionStartTime = entry.getValue();

            // Remove infection after duration
            if (currentTime - infectionStartTime > infectionDuration) {
                return true;
            }

            // Spread to nearby enemy planets every 3 seconds
            if ((currentTime - infectionStartTime) % 3000 < 50) {
                for (Planet nearbyPlanet : game.getPlanets()) {
                    if (nearbyPlanet.getOperator() != operator &&
                            !infectedPlanets.containsKey(nearbyPlanet) &&
                            !newInfections.contains(nearbyPlanet)) {

                        double distance = Math.sqrt(
                                Math.pow(nearbyPlanet.getX() - infectedPlanet.getX(), 2) +
                                        Math.pow(nearbyPlanet.getY() - infectedPlanet.getY(), 2));

                        if (distance < 100) { // Infection spread range
                            newInfections.add(nearbyPlanet);
                        }
                    }
                }
            }

            // Mark planet for damage if it's time (roughly every second)
            if ((currentTime - infectionStartTime) % 1000 < 50) {
                planetsToDamage.add(infectedPlanet);
            }

            return false;
        });

        // Deal damage to infected planets after map modification is complete
        for (Planet planet : planetsToDamage) {
            Ship infectionDamage = new Ship(operator, null, planet, 0, 1, spreadDamage);
            planet.takeDamage(infectionDamage);
        }

        // Add new infections
        for (Planet newInfection : newInfections) {
            infectedPlanets.put(newInfection, currentTime);
        }
    }

    /**
     * Resets all ability cooldowns and active effects for a new game
     */
    public void resetAllAbilities() {
        // Clear all cooldowns
        for (AbilityType type : AbilityType.values()) {
            cooldowns.put(type, 0L);
            activeEffects.put(type, 0L);
        }

        // Reset all active effect flags
        freezeActive = false;
        shieldActive = false;
        factoryHypeActive = false;
        improvedFactoriesActive = false;
        planetaryFlameActive = false;
        lastHealingTime = 0;

        // Clear new ability data
        cursedPlanets.clear();
        blackHoles.clear();
        infectedPlanets.clear();
        unstoppableShipsActive = false;
        orbitalFrozenPlanets.clear();

        // Clear operator-specific data
        operatorFreezeActive.clear();
        operatorShieldActive.clear();
        operatorFactoryHypeActive.clear();
        operatorImprovedFactoriesActive.clear();
        operatorCursedPlanets.clear();
        operatorBlackHoles.clear();
        operatorPlanetaryFlameActive.clear();
        operatorInfectedPlanets.clear();
        operatorUnstoppableShipsActive.clear();
        operatorOrbitalFrozenPlanets.clear();

        // Clear operator expiry times
        operatorFreezeExpiry.clear();
        operatorShieldExpiry.clear();
        operatorFactoryHypeExpiry.clear();
        operatorImprovedFactoriesExpiry.clear();
        operatorPlanetaryFlameExpiry.clear();
        operatorUnstoppableShipsExpiry.clear();
        operatorOrbitalFreezeExpiry.clear();

        // Clear operator expiry times (duplicate - remove this section)
        operatorFreezeExpiry.clear();
        operatorShieldExpiry.clear();
        operatorFactoryHypeExpiry.clear();
        operatorImprovedFactoriesExpiry.clear();
        operatorPlanetaryFlameExpiry.clear();
        operatorUnstoppableShipsExpiry.clear();
        operatorOrbitalFreezeExpiry.clear();
    }

    // ===================== OPERATOR-SPECIFIC ABILITY METHODS =====================

    /**
     * Activates an ability for a specific operator (used by bots)
     */
    public void activateOperatorAbility(Operator operator, AbilityType type, double duration, int power) {
        switch (type) {
            case FREEZE:
                activateOperatorFreeze(operator, duration);
                break;
            case MISSILE_BARRAGE:
                activateOperatorMissileBarrage(operator, power);
                break;
            case SHIELD:
                activateOperatorShield(operator, duration);
                break;
            case FACTORY_HYPE:
                activateOperatorFactoryHype(operator, duration);
                break;
            case IMPROVED_FACTORIES:
                activateOperatorImprovedFactories(operator, duration);
                break;
            case ANSWERED_PRAYERS:
                activateOperatorAnsweredPrayers(operator, power);
                break;
            case CURSE:
                activateOperatorCurse(operator, duration, power);
                break;
            case BLACK_HOLE:
                activateOperatorBlackHole(operator, duration, power);
                break;
            case PLANETARY_FLAME:
                activateOperatorPlanetaryFlame(operator, duration, power);
                break;
            case PLANETARY_INFECTION:
                activateOperatorPlanetaryInfection(operator, duration, power);
                break;
            case UNSTOPPABLE_SHIPS:
                activateOperatorUnstoppableShips(operator, duration, power);
                break;
            case ORBITAL_FREEZE:
                activateOperatorOrbitalFreeze(operator, duration, power);
                break;
        }
    }

    private void activateOperatorFreeze(Operator operator, double duration) {
        operatorFreezeActive.put(operator, true);
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        operatorFreezeExpiry.put(operator, endTime);
    }

    private void activateOperatorMissileBarrage(Operator operator, int missileCount) {
        List<Planet> targetPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() != operator) {
                targetPlanets.add(planet);
            }
        }

        if (targetPlanets.isEmpty())
            return;

        for (int i = 0; i < missileCount; i++) {
            Planet target = targetPlanets.get(random.nextInt(targetPlanets.size()));
            // Create a missile ship that will attack the planet
            int missileDamage = (int) (GameConstants.getDefaultShipDamage()
                    * GameConstants.getMissileDamageMultiplier());

            Ship missile = new Ship(operator, null, target,
                    GameConstants.getProjectileSpeed() * GameConstants.getMissileSpeedMultiplier(), 1, missileDamage,
                    true); // Mark as missile
            missile.setLocation(GameConstants.getGameWidth() / 2, GameConstants.getGameHeight() / 2);
            game.addShip(missile);
            operator.addShip(missile);
        }
    }

    private void activateOperatorShield(Operator operator, double duration) {
        operatorShieldActive.put(operator, true);
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        operatorShieldExpiry.put(operator, endTime);
    }

    private void activateOperatorFactoryHype(Operator operator, double duration) {
        operatorFactoryHypeActive.put(operator, true);
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        operatorFactoryHypeExpiry.put(operator, endTime);
    }

    private void activateOperatorImprovedFactories(Operator operator, double duration) {
        operatorImprovedFactoriesActive.put(operator, true);
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        operatorImprovedFactoriesExpiry.put(operator, endTime);
    }

    private void activateOperatorAnsweredPrayers(Operator operator, int healingPercent) {
        int actualHealing = Math.min(100, healingPercent);

        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() == operator) {
                int maxHealth = planet.getMaxHealth();
                int healAmount = (int) (maxHealth * (actualHealing / 100.0));
                healAmount = Math.min(healAmount, maxHealth - planet.getHealth());
                // Use takeDamage with negative value to heal
                Ship healingShip = new Ship(operator, planet, planet, 0, 1, healAmount);
                planet.takeDamage(healingShip);
            }
        }
    }

    private void activateOperatorCurse(Operator operator, double duration, int statReduction) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);

        // Initialize operator's cursed planets map if not exists
        if (!operatorCursedPlanets.containsKey(operator)) {
            operatorCursedPlanets.put(operator, new HashMap<>());
        }

        // Curse all enemy planets
        Map<Planet, Long> cursedByOperator = operatorCursedPlanets.get(operator);
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() != operator) {
                cursedByOperator.put(planet, endTime);
            }
        }
    }

    private void activateOperatorBlackHole(Operator operator, double duration, int eventHorizon) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);

        // Cap event horizon size
        int cappedHorizon = Math.min(150, eventHorizon);

        // Find a planet owned by this operator to spawn the black hole in orbit
        List<Planet> operatorPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() == operator) {
                operatorPlanets.add(planet);
            }
        }

        double blackHoleX, blackHoleY;

        if (!operatorPlanets.isEmpty()) {
            // Choose a random planet owned by the operator
            Planet orbitPlanet = operatorPlanets.get(random.nextInt(operatorPlanets.size()));

            // Calculate orbit distance (planet radius + black hole radius + some spacing)
            double orbitDistance = orbitPlanet.getActualRadius() + cappedHorizon / 2.0 + 30;

            // Generate random angle for orbit position
            double angle = random.nextDouble() * 2 * Math.PI;

            // Calculate black hole position in orbit
            blackHoleX = orbitPlanet.getX() + Math.cos(angle) * orbitDistance;
            blackHoleY = orbitPlanet.getY() + Math.sin(angle) * orbitDistance;
        } else {
            // Fallback: if no operator planets exist, spawn at random location
            int margin = cappedHorizon / 2 + 50;
            blackHoleX = margin + random.nextDouble() * (GameConstants.getGameWidth() - 2 * margin);
            blackHoleY = margin + random.nextDouble() * (GameConstants.getGameHeight() - 2 * margin);
        }

        // Initialize operator's black holes list if not exists
        if (!operatorBlackHoles.containsKey(operator)) {
            operatorBlackHoles.put(operator, new ArrayList<>());
        }

        operatorBlackHoles.get(operator).add(new BlackHole(blackHoleX, blackHoleY, cappedHorizon, endTime, operator));
    }

    private void activateOperatorPlanetaryFlame(Operator operator, double duration, int flamePower) {
        operatorPlanetaryFlameActive.put(operator, true);
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        operatorPlanetaryFlameExpiry.put(operator, endTime);
    }

    private void activateOperatorPlanetaryInfection(Operator operator, double duration, int power) {
        // Initialize operator's infected planets map if not exists
        if (!operatorInfectedPlanets.containsKey(operator)) {
            operatorInfectedPlanets.put(operator, new HashMap<>());
        }

        // Start infection on one random enemy planet
        long currentTime = System.currentTimeMillis();
        List<Planet> enemyPlanets = new ArrayList<>();

        // Collect all enemy planets
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() != operator) {
                enemyPlanets.add(planet);
            }
        }

        // Infect one random enemy planet if any exist
        if (!enemyPlanets.isEmpty()) {
            Planet targetPlanet = enemyPlanets.get(random.nextInt(enemyPlanets.size()));
            operatorInfectedPlanets.get(operator).put(targetPlanet, currentTime);
        }
    }

    private void activateOperatorUnstoppableShips(Operator operator, double duration, int power) {
        operatorUnstoppableShipsActive.put(operator, true);
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        operatorUnstoppableShipsExpiry.put(operator, endTime);
    }

    private void activateOperatorOrbitalFreeze(Operator operator, double duration, int power) {
        long endTime = System.currentTimeMillis() + (long) (duration * 1000);
        operatorOrbitalFreezeExpiry.put(operator, endTime);

        // Initialize operator's orbital frozen planets map if not exists
        if (!operatorOrbitalFrozenPlanets.containsKey(operator)) {
            operatorOrbitalFrozenPlanets.put(operator, new HashMap<>());
        }

        // Get all enemy planets and freeze the first 'power' number of them
        List<Planet> enemyPlanets = new ArrayList<>();
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() != operator) {
                enemyPlanets.add(planet);
            }
        }

        // Freeze up to 'power' planets
        int planetsToFreeze = Math.min(power, enemyPlanets.size());
        Map<Planet, Long> frozenByOperator = operatorOrbitalFrozenPlanets.get(operator);
        for (int i = 0; i < planetsToFreeze; i++) {
            Planet planet = enemyPlanets.get(i);
            frozenByOperator.put(planet, endTime);
        }
    }

    // Getters for operator-specific abilities
    public boolean isOperatorFreezeActive(Operator operator) {
        return operatorFreezeActive.getOrDefault(operator, false);
    }

    public boolean isOperatorShieldActive(Operator operator) {
        return operatorShieldActive.getOrDefault(operator, false);
    }

    public boolean isOperatorFactoryHypeActive(Operator operator) {
        return operatorFactoryHypeActive.getOrDefault(operator, false);
    }

    public boolean isOperatorImprovedFactoriesActive(Operator operator) {
        return operatorImprovedFactoriesActive.getOrDefault(operator, false);
    }

    public boolean isOperatorPlanetaryFlameActive(Operator operator) {
        return operatorPlanetaryFlameActive.getOrDefault(operator, false);
    }

    public boolean isOperatorUnstoppableShipsActive(Operator operator) {
        return operatorUnstoppableShipsActive.getOrDefault(operator, false);
    }

    public Map<Planet, Long> getOperatorOrbitalFrozenPlanets(Operator operator) {
        return operatorOrbitalFrozenPlanets.getOrDefault(operator, new HashMap<>());
    }

    public boolean isOperatorPlanetOrbitallyFrozen(Operator operator, Planet planet) {
        Map<Planet, Long> frozenByOperator = operatorOrbitalFrozenPlanets.get(operator);
        if (frozenByOperator == null)
            return false;
        return frozenByOperator.containsKey(planet) &&
                System.currentTimeMillis() < frozenByOperator.get(planet);
    }

    public boolean isPlanetOrbitallyFrozen(Planet planet) {
        // Check if planet is frozen by player
        Long playerFreezeEnd = orbitalFrozenPlanets.get(planet);
        if (playerFreezeEnd != null && System.currentTimeMillis() < playerFreezeEnd) {
            return true;
        }

        // Check if planet is frozen by any operator
        for (Map<Planet, Long> frozenByOperator : operatorOrbitalFrozenPlanets.values()) {
            Long freezeEnd = frozenByOperator.get(planet);
            if (freezeEnd != null && System.currentTimeMillis() < freezeEnd) {
                return true;
            }
        }

        return false;
    }

    public Map<Planet, Long> getOperatorCursedPlanets(Operator operator) {
        return operatorCursedPlanets.getOrDefault(operator, new HashMap<>());
    }

    public Map<Planet, Long> getOperatorInfectedPlanets(Operator operator) {
        return operatorInfectedPlanets.getOrDefault(operator, new HashMap<>());
    }

    public List<BlackHole> getOperatorBlackHoles(Operator operator) {
        return operatorBlackHoles.getOrDefault(operator, new ArrayList<>());
    }

    public boolean isOperatorPlanetCursed(Operator operator, Planet planet) {
        Map<Planet, Long> cursedByOperator = operatorCursedPlanets.get(operator);
        if (cursedByOperator == null)
            return false;
        return cursedByOperator.containsKey(planet) &&
                System.currentTimeMillis() < cursedByOperator.get(planet);
    }

    public boolean isOperatorPlanetInfected(Operator operator, Planet planet) {
        Map<Planet, Long> infectedByOperator = operatorInfectedPlanets.get(operator);
        if (infectedByOperator == null)
            return false;
        return infectedByOperator.containsKey(planet);
    }
}