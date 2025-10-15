package home.game.io.datacontainers;

import java.util.HashMap;
import java.util.Map;
import home.game.abilities.AbilityType;
import home.game.operators.Difficulty;
import home.game.operators.player.UpgradeType;

public class PlayerDataContainer {
    public final int coins;
    public final int achievementScore;
    public final Map<Difficulty, Long> bestTimes;
    public final Map<UpgradeType, Integer> upgradeLevels;
    public final Map<AbilityType, Boolean> abilitiesUnlocked;
    public final Map<AbilityType, Integer> abilityLevels;

    public PlayerDataContainer() {
        this.coins = 0;
        this.achievementScore = 0;
        this.bestTimes = new HashMap<>();
        this.upgradeLevels = new HashMap<>();
        this.abilitiesUnlocked = new HashMap<>();
        this.abilityLevels = new HashMap<>();

        // Initialize with defaults
        for (UpgradeType type : UpgradeType.values()) {
            upgradeLevels.put(type, 0);
        }
        for (AbilityType type : AbilityType.values()) {
            abilitiesUnlocked.put(type, false);
            abilityLevels.put(type, 0);
        }
    }

    public PlayerDataContainer(int coins, int achievementScore, Map<Difficulty, Long> bestTimes,
            Map<UpgradeType, Integer> upgradeLevels, Map<AbilityType, Boolean> abilitiesUnlocked,
            Map<AbilityType, Integer> abilityLevels) {
        this.coins = coins;
        this.achievementScore = achievementScore;
        this.bestTimes = bestTimes;
        this.upgradeLevels = upgradeLevels;
        this.abilitiesUnlocked = abilitiesUnlocked;
        this.abilityLevels = abilityLevels;
    }
}
