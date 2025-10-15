package home.game.operators.player;

// Upgrade types enum
public enum UpgradeType {
    DOUBLE_SHIP_CHANCE("Double Ship Chance", "%", 50, 1.22),
    SHIP_DAMAGE("Ship Damage", "%", 15, 1.15),
    SHIP_HEALTH("Ship Health", "%", 12, 1.14),
    PLANET_HEALTH("Planet Health", "%", 20, 1.17),
    PLANET_DAMAGE_REDUCTION("Planet Damage Reduction", "%", 25, 1.18),
    SHIP_SPEED("Ship Speed", "%", 16, 1.13),
    SHIP_SPAWN_SPEED("Ship Spawn Speed", "%", 18, 1.15),
    ABILITY_COOLDOWN("Ability Cooldown Reduction", "%", 30, 1.20);

    private final String displayName;
    private final String unit;
    private final int baseCost;
    private final double growthRate;

    UpgradeType(String displayName, String unit, int baseCost, double growthRate) {
        this.displayName = displayName;
        this.unit = unit;
        this.baseCost = baseCost;
        this.growthRate = growthRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    /**
     * Calculate the cost for upgrading to the next level
     * Formula: baseCost * (growthRate^currentLevel)
     */
    public int calculateCost(int currentLevel) {
        if (currentLevel >= 30)
            return -1; // Max level reached

        return (int) Math.round(baseCost * Math.pow(growthRate, currentLevel));
    }

    /**
     * Calculate the actual value/multiplier for this upgrade at given level
     */
    public double calculateValue(int level) {
        return switch (this) {
            case DOUBLE_SHIP_CHANCE -> Math.min(50.0, level * 2.0); // Max 50% at level 25
            case SHIP_DAMAGE -> level * 5.0; // 5% per level
            case SHIP_HEALTH -> level * 10.0; // 10% per level
            case PLANET_HEALTH -> level * 15.0; // 15% per level
            case PLANET_DAMAGE_REDUCTION -> Math.min(75.0, level * 3.0); // Max 75% at level 25
            case SHIP_SPEED -> level * 4.0; // 4% per level
            case SHIP_SPAWN_SPEED -> level * 6.0; // 6% per level
            case ABILITY_COOLDOWN -> Math.min(50.0, level * 2.0); // Max 50% at level 25
        };
    }
}