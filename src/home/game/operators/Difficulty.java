package home.game.operators;

public enum Difficulty {
    EASY(3000, .5, 0.8, 1, 2, 5, 8, 0.3, false, .05),
    MEDIUM(2000, .7, 1.0, 2, 3, 8, 12, 0.5, false, .15),
    HARD(1200, 1.1, 1.3, 3, 5, 12, 16, 0.7, true, .3),
    EXTREME(800, 1.8, 1.8, 4, 6, 16, 20, 0.8, true, .65);

    private final long botDecisionInterval; // Milliseconds between bot decisions
    private final double botAggressiveness; // Multiplier for bot aggression
    private final double botEfficiency; // Multiplier for bot efficiency/targeting
    private final int minBots; // Minimum number of bots
    private final int maxBots; // Maximum number of bots
    private final int minPlanets; // Minimum number of planets
    private final int maxPlanets; // Maximum number of planets
    private final double enemyPlanetRatio; // Ratio of planets that should be enemy-owned (0.0-1.0)
    private final boolean botsGetAbilities; // Whether bots get abilities and upgrades
    private final double advancedPlanetChance; // Chance for advanced planet types (0.0-1.0)

    Difficulty(long botDecisionInterval, double botAggressiveness, double botEfficiency, int minBots, int maxBots,
            int minPlanets, int maxPlanets, double enemyPlanetRatio, boolean botsGetAbilities,
            double advancedPlanetChance) {
        this.botDecisionInterval = botDecisionInterval;
        this.botAggressiveness = botAggressiveness;
        this.botEfficiency = botEfficiency;
        this.minBots = minBots;
        this.maxBots = maxBots;
        this.minPlanets = minPlanets;
        this.maxPlanets = maxPlanets;
        this.enemyPlanetRatio = enemyPlanetRatio;
        this.botsGetAbilities = botsGetAbilities;
        this.advancedPlanetChance = advancedPlanetChance;
    }

    public double getAdvancedPlanetChance() {
        return advancedPlanetChance;
    }

    public long getBotDecisionInterval() {
        return botDecisionInterval;
    }

    public double getBotAggressiveness() {
        return botAggressiveness;
    }

    public double getBotEfficiency() {
        return botEfficiency;
    }

    public int getMinBots() {
        return minBots;
    }

    public int getMaxBots() {
        return maxBots;
    }

    public int getMinPlanets() {
        return minPlanets;
    }

    public int getMaxPlanets() {
        return maxPlanets;
    }

    public double getEnemyPlanetRatio() {
        return enemyPlanetRatio;
    }

    public boolean getBotsGetAbilities() {
        return botsGetAbilities;
    }
}