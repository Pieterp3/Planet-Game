package home.game.io.datacontainers;

import java.util.HashMap;
import java.util.Map;

import home.game.abilities.AbilityType;

public class ChallengeData {
    public final int totalPlanetsCaptured;
    public final int totalAbilitiesUsed;
    public final int totalGoldDonated;
    public final Map<AbilityType, Integer> specificAbilityUsage;

    public ChallengeData() {
        this.totalPlanetsCaptured = 0;
        this.totalAbilitiesUsed = 0;
        this.totalGoldDonated = 0;
        this.specificAbilityUsage = new HashMap<>();
    }

    public ChallengeData(int totalPlanetsCaptured, int totalAbilitiesUsed, int totalGoldDonated,
            Map<AbilityType, Integer> specificAbilityUsage) {
        this.totalPlanetsCaptured = totalPlanetsCaptured;
        this.totalAbilitiesUsed = totalAbilitiesUsed;
        this.totalGoldDonated = totalGoldDonated;
        this.specificAbilityUsage = specificAbilityUsage;
    }
}
