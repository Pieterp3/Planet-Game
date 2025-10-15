package home.game.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import home.game.Game;
import home.game.abilities.AbilityManager;
import home.game.abilities.AbilityType;
import home.game.operators.player.UpgradeType;
import home.game.planets.Planet;

public class Bot extends Operator {

    private long lastDecisionTime = 0;
    private long decisionInterval; // Dynamic based on difficulty
    private static final long INITIAL_DELAY = 5000; // Initial delay before first decision
    private long startTime = System.currentTimeMillis();
    private double aggressiveness; // How aggressive this bot is
    private double efficiency; // How efficient this bot is at targeting

    // Bot abilities and upgrades (only available on higher difficulties)
    private List<AbilityType> botAbilities;
    private List<UpgradeType> botUpgrades;
    private long lastAbilityUse = 0;
    private static final long ABILITY_COOLDOWN = 15000; // 15 seconds between ability uses

    // Ability effect durations for visual tracking
    private long botShieldEndTime = 0;
    private long botFactoryHypeEndTime = 0;
    private long botImprovedFactoriesEndTime = 0;
    private long botBlackHoleEndTime = 0;
    private long botPlanetaryFlameEndTime = 0;
    private long botFreezeEndTime = 0;
    private long botMissileBarrageEndTime = 0;
    private long botAnsweredPrayersEndTime = 0;
    private long botCurseEndTime = 0;
    private long botUnstoppableShipsEndTime = 0;
    private long botOrbitalFreezeEndTime = 0;

    public Bot(Game game) {
        super(game);
        // Set difficulty-based parameters
        Difficulty difficulty = game.getDifficulty();
        this.decisionInterval = difficulty.getBotDecisionInterval();
        this.aggressiveness = difficulty.getBotAggressiveness();
        this.efficiency = difficulty.getBotEfficiency();

        // Initialize abilities and upgrades
        this.botAbilities = new ArrayList<>();
        this.botUpgrades = new ArrayList<>();

        // Grant abilities and upgrades based on difficulty
        if (difficulty.getBotsGetAbilities()) {
            grantBotAbilitiesAndUpgrades(difficulty);
        }
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void tick() {
        if (System.currentTimeMillis() - startTime < INITIAL_DELAY) {
            return; // Wait for initial delay before starting decisions
        }
        long currentTime = System.currentTimeMillis();

        // Only make decisions at intervals to avoid spam
        if (currentTime - lastDecisionTime < decisionInterval) {
            return;
        }
        lastDecisionTime = currentTime;

        // Update bot ability effects
        updateBotAbilityEffects(currentTime);

        // Try to use abilities (for bots on higher difficulties)
        tryUseAbility();

        // Get all planets controlled by this bot
        List<Planet> myPlanets = getMyPlanets();

        if (myPlanets.isEmpty()) {
            return; // Bot has no planets, nothing to do
        }

        // First priority: Reinforce low-health planets if we have multiple planets
        if (myPlanets.size() > 1) {
            reinforceLowHealthPlanets(myPlanets);
        }

        // Second priority: Form connections to attack other planets
        formAttackConnections(myPlanets);
    }

    /**
     * Gets all planets controlled by this bot
     */
    private List<Planet> getMyPlanets() {
        List<Planet> myPlanets = new ArrayList<>();
        for (Planet planet : getGame().getPlanets()) {
            if (planet.getOperator() == this) {
                myPlanets.add(planet);
            }
        }
        return myPlanets;
    }

    /**
     * Reinforces planets that have low health by connecting stronger planets to
     * them
     */
    private void reinforceLowHealthPlanets(List<Planet> myPlanets) {
        for (Planet planet : myPlanets) {
            // Adjust health threshold based on efficiency (more efficient = better resource
            // management)
            double healthThreshold = 0.5 + (efficiency - 1.0) * 0.2; // Range from ~0.3 to ~0.7

            // Consider a planet "low health" based on difficulty-adjusted threshold
            if (planet.getHealth() < planet.getMaxHealth() * healthThreshold) {
                // Find a stronger planet to reinforce this one
                Planet reinforcer = findBestReinforcer(planet, myPlanets);
                if (reinforcer != null) {
                    // Check if reinforcer already has this planet as target
                    if (!reinforcer.getTargets().contains(planet)) {
                        reinforcer.attemptTargeting(planet);
                    }
                }
            }
        }
    }

    /**
     * Finds the best planet to use as a reinforcer for a weak planet
     */
    private Planet findBestReinforcer(Planet weakPlanet, List<Planet> myPlanets) {
        Planet bestReinforcer = null;
        int bestHealth = 0;

        for (Planet planet : myPlanets) {
            if (planet == weakPlanet)
                continue; // Can't reinforce itself

            // Prefer planets with high health that aren't already targeting too many
            // planets
            if (planet.getHealth() > planet.getMaxHealth() * 0.8 &&
                    planet.getTargets().size() < 2 &&
                    planet.getHealth() > bestHealth) {
                bestReinforcer = planet;
                bestHealth = planet.getHealth();
            }
        }

        return bestReinforcer;
    }

    /**
     * Forms connections to attack enemy or neutral planets
     */
    private void formAttackConnections(List<Planet> myPlanets) {
        // Get all enemy/neutral planets
        List<Planet> enemyPlanets = new ArrayList<>();
        for (Planet planet : getGame().getPlanets()) {
            if (planet.getOperator() != this) {
                enemyPlanets.add(planet);
            }
        }

        if (enemyPlanets.isEmpty()) {
            return; // No enemies to attack
        }

        // For each of our planets, try to add attack targets
        for (Planet myPlanet : myPlanets) {
            // Adjust health threshold based on aggressiveness (more aggressive = attack
            // with lower health)
            double healthThreshold = 0.3 / aggressiveness;
            // Adjust max targets based on aggressiveness (more aggressive = more
            // simultaneous attacks)
            int maxTargets = (int) Math.ceil(2 * aggressiveness);

            // Skip if planet is too weak or already has many targets
            if (myPlanet.getHealth() < myPlanet.getMaxHealth() * healthThreshold ||
                    myPlanet.getTargets().size() >= maxTargets) {
                continue;
            }

            // Find the best enemy planet to target
            Planet target = findBestAttackTarget(myPlanet, enemyPlanets);
            if (target != null && !myPlanet.getTargets().contains(target)) {
                myPlanet.attemptTargeting(target);
            }
        }
    }

    /**
     * Finds the best enemy planet to attack from a given planet
     */
    private Planet findBestAttackTarget(Planet attackerPlanet, List<Planet> enemyPlanets) {
        Planet bestTarget = null;
        double bestScore = -1;

        for (Planet enemy : enemyPlanets) {
            // Calculate distance
            double distance = Math.hypot(
                    attackerPlanet.getX() - enemy.getX(),
                    attackerPlanet.getY() - enemy.getY());

            // Calculate a score based on distance (closer is better) and enemy health
            // (weaker is better)
            // Apply efficiency multiplier to make targeting more strategic at higher
            // difficulties
            double distanceScore = (1000.0 / (distance + 1)) * efficiency;
            double healthScore = (1000.0 / (enemy.getHealth() + 1)) * efficiency;

            // Prioritize player planets more at higher difficulties
            boolean isPlayerPlanet = enemy.getOperator() == getGame().getPlayer();
            double playerBonus = isPlayerPlanet ? (aggressiveness * 3.0) : 0.0;

            double score = distanceScore + healthScore + playerBonus;

            if (score > bestScore) {
                bestScore = score;
                bestTarget = enemy;
            }
        }

        return bestTarget;
    }

    /**
     * Grants abilities and upgrades to the bot based on difficulty level
     */
    private void grantBotAbilitiesAndUpgrades(Difficulty difficulty) {
        Random random = new Random();

        // Define available abilities for bots (all abilities except
        // PLANETARY_INFECTION)
        AbilityType[] availableAbilities = {
                AbilityType.FREEZE,
                AbilityType.MISSILE_BARRAGE,
                AbilityType.SHIELD,
                AbilityType.FACTORY_HYPE,
                AbilityType.IMPROVED_FACTORIES,
                AbilityType.ANSWERED_PRAYERS,
                AbilityType.CURSE,
                AbilityType.BLACK_HOLE,
                AbilityType.PLANETARY_FLAME,
                AbilityType.UNSTOPPABLE_SHIPS
        };

        // Define available upgrades for bots
        UpgradeType[] availableUpgrades = {
                UpgradeType.SHIP_DAMAGE,
                UpgradeType.SHIP_HEALTH,
                UpgradeType.SHIP_SPEED,
                UpgradeType.SHIP_SPAWN_SPEED,
                UpgradeType.PLANET_HEALTH
        };

        // Grant abilities based on difficulty
        int numAbilities;
        switch (difficulty) {
            case HARD:
                numAbilities = 1 + random.nextInt(2); // 1-2 abilities
                break;
            case EXTREME:
                numAbilities = 2 + random.nextInt(3); // 2-4 abilities
                break;
            default:
                numAbilities = 0; // No abilities for EASY/MEDIUM
                break;
        }

        // Randomly select abilities
        List<AbilityType> selectedAbilities = new ArrayList<>();
        for (int i = 0; i < numAbilities && selectedAbilities.size() < availableAbilities.length; i++) {
            AbilityType ability = availableAbilities[random.nextInt(availableAbilities.length)];
            if (!selectedAbilities.contains(ability)) {
                selectedAbilities.add(ability);
            }
        }
        this.botAbilities = selectedAbilities;

        // Grant upgrades based on difficulty
        int numUpgrades;
        switch (difficulty) {
            case HARD:
                numUpgrades = 2 + random.nextInt(3); // 2-4 upgrades
                break;
            case EXTREME:
                numUpgrades = 4 + random.nextInt(2); // 4-5 upgrades
                break;
            default:
                numUpgrades = 0; // No upgrades for EASY/MEDIUM
                break;
        }

        // Randomly select upgrades
        List<UpgradeType> selectedUpgrades = new ArrayList<>();
        for (int i = 0; i < numUpgrades && selectedUpgrades.size() < availableUpgrades.length; i++) {
            UpgradeType upgrade = availableUpgrades[random.nextInt(availableUpgrades.length)];
            if (!selectedUpgrades.contains(upgrade)) {
                selectedUpgrades.add(upgrade);
            }
        }
        this.botUpgrades = selectedUpgrades;
    }

    /**
     * Gets the bot's abilities
     */
    public List<AbilityType> getBotAbilities() {
        return new ArrayList<>(botAbilities);
    }

    /**
     * Gets the bot's upgrades
     */
    public List<UpgradeType> getBotUpgrades() {
        return new ArrayList<>(botUpgrades);
    }

    /**
     * Gets the upgrade multiplier for a specific upgrade type
     * Bots get a fixed bonus per upgrade they have
     */
    public double getBotUpgradeMultiplier(UpgradeType type) {
        AbilityManager abilityManager = getGame().getAbilityManager();

        if (!botUpgrades.contains(type)) {
            double baseMultiplier = 1.0; // No upgrade = no bonus

            // Apply temporary ability bonuses
            if (type == UpgradeType.SHIP_SPAWN_SPEED && abilityManager.isOperatorFactoryHypeActive(this)) {
                baseMultiplier *= 2.0; // Factory Hype doubles spawn speed
            }
            if ((type == UpgradeType.SHIP_HEALTH || type == UpgradeType.SHIP_DAMAGE ||
                    type == UpgradeType.SHIP_SPEED)
                    && abilityManager.isOperatorImprovedFactoriesActive(this)) {
                baseMultiplier *= 2.0; // Improved Factories doubles ship stats
            }

            return baseMultiplier;
        }

        // Bots get a fixed 50% bonus for each upgrade they have, plus ability bonuses
        double multiplier = 1.5;

        // Apply temporary ability bonuses
        if (type == UpgradeType.SHIP_SPAWN_SPEED && abilityManager.isOperatorFactoryHypeActive(this)) {
            multiplier *= 2.0; // Factory Hype doubles spawn speed
        }
        if ((type == UpgradeType.SHIP_HEALTH || type == UpgradeType.SHIP_DAMAGE ||
                type == UpgradeType.SHIP_SPEED) && abilityManager.isOperatorImprovedFactoriesActive(this)) {
            multiplier *= 2.0; // Improved Factories doubles ship stats
        }

        return multiplier;
    }

    /**
     * Checks if bot has a specific upgrade
     */
    public boolean hasBotUpgrade(UpgradeType type) {
        return botUpgrades.contains(type);
    }

    /**
     * Updates bot ability effects and removes expired ones
     */
    private void updateBotAbilityEffects(long currentTime) {
        // Visual effect timing is still handled locally for rendering
        // Actual ability effects are handled by AbilityManager
    }

    /**
     * Checks if bot shield is active (for damage reduction)
     */
    public boolean isBotShieldActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return abilityManager.isOperatorShieldActive(this) || System.currentTimeMillis() < botShieldEndTime;
    }

    /**
     * Checks if bot factory hype is active (for visual effects)
     */
    public boolean isBotFactoryHypeActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return abilityManager.isOperatorFactoryHypeActive(this) || System.currentTimeMillis() < botFactoryHypeEndTime;
    }

    /**
     * Checks if bot improved factories is active (for visual effects)
     */
    public boolean isBotImprovedFactoriesActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return abilityManager.isOperatorImprovedFactoriesActive(this)
                || System.currentTimeMillis() < botImprovedFactoriesEndTime;
    }

    /**
     * Checks if bot black hole is active (for visual effects)
     */
    public boolean isBotBlackHoleActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return !abilityManager.getOperatorBlackHoles(this).isEmpty()
                || System.currentTimeMillis() < botBlackHoleEndTime;
    }

    /**
     * Checks if bot planetary flame is active (for visual effects)
     */
    public boolean isBotPlanetaryFlameActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return abilityManager.isOperatorPlanetaryFlameActive(this)
                || System.currentTimeMillis() < botPlanetaryFlameEndTime;
    }

    /**
     * Checks if bot freeze is active (for visual effects)
     */
    public boolean isBotFreezeActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return abilityManager.isOperatorFreezeActive(this) || System.currentTimeMillis() < botFreezeEndTime;
    }

    /**
     * Checks if bot missile barrage is active (for visual effects)
     */
    public boolean isBotMissileBarrageActive() {
        return System.currentTimeMillis() < botMissileBarrageEndTime;
    }

    /**
     * Checks if bot answered prayers is active (for visual effects)
     */
    public boolean isBotAnsweredPrayersActive() {
        return System.currentTimeMillis() < botAnsweredPrayersEndTime;
    }

    /**
     * Checks if bot curse is active (for visual effects)
     */
    public boolean isBotCurseActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return !abilityManager.getOperatorCursedPlanets(this).isEmpty() || System.currentTimeMillis() < botCurseEndTime;
    }

    /**
     * Checks if bot unstoppable ships is active (for visual effects)
     */
    public boolean isBotUnstoppableShipsActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return abilityManager.isOperatorUnstoppableShipsActive(this)
                || System.currentTimeMillis() < botUnstoppableShipsEndTime;
    }

    /**
     * Checks if bot orbital freeze is active (for visual effects)
     */
    public boolean isBotOrbitalFreezeActive() {
        AbilityManager abilityManager = getGame().getAbilityManager();
        return !abilityManager.getOperatorOrbitalFrozenPlanets(this).isEmpty()
                || System.currentTimeMillis() < botOrbitalFreezeEndTime;
    }

    /**
     * Attempts to use a random ability if available and not on cooldown
     */
    public void tryUseAbility() {
        long currentTime = System.currentTimeMillis();

        if (botAbilities.isEmpty() || currentTime - lastAbilityUse < ABILITY_COOLDOWN) {
            return; // No abilities or still on cooldown
        }

        // Randomly select an ability to use
        Random random = new Random();
        AbilityType selectedAbility = botAbilities.get(random.nextInt(botAbilities.size()));

        // Use bot-specific ability activation (doesn't affect player cooldowns)
        useBotAbility(selectedAbility);
        lastAbilityUse = currentTime;
    }

    /**
     * Activates bot abilities using the AbilityManager
     */
    private void useBotAbility(AbilityType ability) {
        List<Planet> myPlanets = getMyPlanets();
        if (myPlanets.isEmpty()) {
            return; // No planets to apply abilities to
        }

        AbilityManager abilityManager = getGame().getAbilityManager();
        long currentTime = System.currentTimeMillis();

        // Set visual effect timings for rendering
        switch (ability) {
            case FREEZE:
                abilityManager.activateOperatorAbility(this, ability, 6.0, 0);
                botFreezeEndTime = currentTime + 6000;
                break;

            case MISSILE_BARRAGE:
                abilityManager.activateOperatorAbility(this, ability, 0.0, 3);
                botMissileBarrageEndTime = currentTime + 4000;
                break;

            case SHIELD:
                abilityManager.activateOperatorAbility(this, ability, 10.0, 0);
                botShieldEndTime = currentTime + 10000;
                break;

            case FACTORY_HYPE:
                abilityManager.activateOperatorAbility(this, ability, 8.0, 0);
                botFactoryHypeEndTime = currentTime + 8000;
                break;

            case IMPROVED_FACTORIES:
                abilityManager.activateOperatorAbility(this, ability, 12.0, 0);
                botImprovedFactoriesEndTime = currentTime + 12000;
                break;

            case CURSE:
                abilityManager.activateOperatorAbility(this, ability, 7.0, 25);
                botCurseEndTime = currentTime + 7000;
                break;

            case UNSTOPPABLE_SHIPS:
                abilityManager.activateOperatorAbility(this, ability, 6.0, 0);
                botUnstoppableShipsEndTime = currentTime + 6000;
                break;

            case ANSWERED_PRAYERS:
                abilityManager.activateOperatorAbility(this, ability, 0.0, 50);
                botAnsweredPrayersEndTime = currentTime + 3000;
                break;

            case BLACK_HOLE:
                abilityManager.activateOperatorAbility(this, ability, 5.0, 100);
                botBlackHoleEndTime = currentTime + 5000;
                break;

            case PLANETARY_FLAME:
                abilityManager.activateOperatorAbility(this, ability, 8.0, 50);
                botPlanetaryFlameEndTime = currentTime + 8000;
                break;

            case PLANETARY_INFECTION:
                abilityManager.activateOperatorAbility(this, ability, 10.0, 30);
                break;

            case ORBITAL_FREEZE:
                abilityManager.activateOperatorAbility(this, ability, 10.0, 2); // 10 second duration, freeze 2 planets
                botOrbitalFreezeEndTime = currentTime + 10000;
                break;
        }
    }

}
