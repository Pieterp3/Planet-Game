package home.game.planets;

import home.game.operators.Difficulty;

public enum PlanetType {

    STANDARD(0, 1.0, 1.0, 1.0, 1.0, 1.0), // Normal planet
    ATTACK(1, 1.2, 1.5, 1.0, 1.0, 1.0), // Increases ship attack power
    DEFENCE(0, 1.0, 1.0, 1.5, 1.5, 1.0), // Increases planet defence
    SPEED(0, 1.5, 1.0, 1.0, 1.0, 1.5); // Increases ship speed

    private final int extraTargets;// handled
    private final double shipSpeedMultiplier;// handled
    private final double attackMultiplier;// handled
    private final double defenceMultiplier;// handled
    private final double healthRegenMultiplier;// handled
    private final double shipProductionMultiplier;// handled

    PlanetType(int extraTargets, double shipSpeedMultiplier, double attackMultiplier,
            double defenceMultiplier, double healthRegenMultiplier, double shipProductionMultiplier) {
        this.extraTargets = extraTargets;
        this.shipSpeedMultiplier = shipSpeedMultiplier;
        this.attackMultiplier = attackMultiplier;
        this.defenceMultiplier = defenceMultiplier;
        this.healthRegenMultiplier = healthRegenMultiplier;
        this.shipProductionMultiplier = shipProductionMultiplier;
    }

    public int getExtraTargets() {
        return extraTargets;
    }

    public double getShipSpeedMultiplier() {
        return shipSpeedMultiplier;
    }

    public double getAttackMultiplier() {
        return attackMultiplier;
    }

    public double getDefenceMultiplier() {
        return defenceMultiplier;
    }

    public double getHealthRegenMultiplier() {
        return healthRegenMultiplier;
    }

    public double getShipProductionMultiplier() {
        return shipProductionMultiplier;
    }

    public static PlanetType getRandomPlanetType(Difficulty difficulty) {
        double chance = Math.random();
        if (chance < difficulty.getAdvancedPlanetChance()) {
            // Randomly return one of the advanced types
            PlanetType[] advancedTypes = { ATTACK, DEFENCE, SPEED };
            int index = (int) (Math.random() * advancedTypes.length);
            return advancedTypes[index];
        }
        return STANDARD;
    }

}
