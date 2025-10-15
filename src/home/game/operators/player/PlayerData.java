package home.game.operators.player;

import java.util.HashMap;
import java.util.Map;

import home.game.Game;
import home.game.GameConstants;
import home.game.abilities.AbilityType;
import home.game.challenges.ChallengeManager;
import home.game.io.SaveLoadManager;
import home.game.io.datacontainers.PlayerDataContainer;
import home.game.operators.Difficulty;

public class PlayerData {
    private static PlayerData instance;

    // Player currency and progression
    private int coins;
    private int achievementScore;

    // Best times for each difficulty (in milliseconds)
    private Map<Difficulty, Long> bestTimes;

    // Upgrade levels (0-based, max ~30 upgrades as specified)
    private Map<UpgradeType, Integer> upgradeLevels;

    // Player abilities - whether they've been unlocked and their upgrade levels
    private Map<AbilityType, Boolean> abilitiesUnlocked;
    private Map<AbilityType, Integer> abilityLevels;

    // Private constructor for singleton pattern
    private PlayerData() {
        this.coins = 0;
        this.achievementScore = 0;
        this.bestTimes = new HashMap<>();
        this.upgradeLevels = new HashMap<>();
        this.abilitiesUnlocked = new HashMap<>();
        this.abilityLevels = new HashMap<>();

        // Initialize all upgrade types to level 0
        for (UpgradeType type : UpgradeType.values()) {
            upgradeLevels.put(type, 0);
        }

        // Initialize all abilities as locked and at level 0
        for (AbilityType type : AbilityType.values()) {
            abilitiesUnlocked.put(type, false);
            abilityLevels.put(type, 0);
        }
    }

    public static PlayerData getInstance() {
        if (instance == null) {
            instance = new PlayerData();
            instance.loadData();
        }
        return instance;
    }

    /**
     * Calculate coin reward based on game duration and difficulty
     * Base reward: 30 seconds of play = 10 coins
     * After 30 upgrades, should need 10 minutes of play time
     * So we need exponential scaling where upgrade 30 costs ~400 coins (10 min / 30
     * sec * 10)
     */
    public int calculateReward(long gameTimeMs, Difficulty difficulty, boolean won, int totalPlanets,
            int uncapturedPlanets) {
        if (!won)
            return 0; // No reward for losing

        return calculatePotentialRewardWithBonus(gameTimeMs, difficulty, totalPlanets, uncapturedPlanets);
    }

    // Backward compatibility method
    public int calculateReward(long gameTimeMs, Difficulty difficulty, boolean won) {
        return calculateReward(gameTimeMs, difficulty, won, 0, 0);
    }

    /**
     * Calculate what the reward would be if the player won right now
     */
    public int calculatePotentialReward(long gameTimeMs, Difficulty difficulty) {
        return calculatePotentialRewardWithBonus(gameTimeMs, difficulty, 0, 0);
    }

    /**
     * Calculate potential reward including aggressive conqueror bonus
     */
    public int calculatePotentialRewardWithBonus(long gameTimeMs, Difficulty difficulty, int totalPlanets,
            int uncapturedPlanets) {
        double baseReward = (gameTimeMs / 1000.0 / 30.0) * GameConstants.getBaseCoinReward(); // 10 coins per 30 seconds

        // Difficulty multipliers
        double difficultyMultiplier = switch (difficulty) {
            case EASY -> GameConstants.getEasyRewardMultiplier();
            case MEDIUM -> GameConstants.getMediumRewardMultiplier();
            case HARD -> GameConstants.getHardRewardMultiplier();
            case EXTREME -> GameConstants.getExtremeRewardMultiplier();
        };

        // Time bonus for faster completion (up to 2x multiplier for very fast games)
        double timeMultiplier = Math.max(1.0, Math.min(2.0, 300.0 / (gameTimeMs / 1000.0))); // 5 min baseline

        // Aggressive Conqueror bonus - bigger bonus for more uncaptured planets
        double aggressiveMultiplier = 1.0;
        if (uncapturedPlanets > 0 && totalPlanets > 0) {
            double uncapturedRatio = uncapturedPlanets / (double) totalPlanets;
            // Bonus increases exponentially with uncaptured ratio: 1.0x to 3.0x
            aggressiveMultiplier = 1.0 + (2.0 * Math.pow(uncapturedRatio, 1.5));
        }

        int baseAmount = (int) Math.round(baseReward * difficultyMultiplier * timeMultiplier);
        int finalReward = (int) Math.round(baseAmount * aggressiveMultiplier);
        return (int) (Math.max(1, finalReward) * GameConstants.getDebugCoinsMultiplier()); // Minimum 1 coin
    }

    /**
     * Calculate upgrade cost with type-specific exponential scaling
     * Each upgrade type has its own base cost and growth rate
     */
    public int getUpgradeCost(UpgradeType type) {
        int currentLevel = getUpgradeLevel(type);
        return type.calculateCost(currentLevel);
    }

    // Getters and setters
    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        this.coins += amount;
        saveData();
    }

    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            saveData();
            return true;
        }
        return false;
    }

    public int getAchievementScore() {
        return achievementScore;
    }

    public void addAchievementScore(int amount) {
        this.achievementScore += amount;
        saveData();
    }

    /**
     * Manually triggers a verification and correction of the achievement score.
     * This can be called externally to ensure the achievement score matches
     * the completed challenges.
     * 
     * @return true if the score was corrected, false if it was already correct
     */
    public boolean verifyAndFixAchievementScore() {
        int correctScore = calculateCorrectAchievementScore();
        if (this.achievementScore != correctScore) {
            this.achievementScore = correctScore;
            saveData();
            return true;
        }
        return false;
    }

    public Long getBestTime(Difficulty difficulty) {
        return bestTimes.get(difficulty);
    }

    public void updateBestTime(Difficulty difficulty, long timeMs) {
        Long currentBest = bestTimes.get(difficulty);
        if (currentBest == null || timeMs < currentBest) {
            bestTimes.put(difficulty, timeMs);
            saveData();
        }
    }

    public int getUpgradeLevel(UpgradeType type) {
        return upgradeLevels.getOrDefault(type, 0);
    }

    public boolean purchaseUpgrade(UpgradeType type) {
        int cost = getUpgradeCost(type);
        if (cost > 0 && spendCoins(cost)) {
            upgradeLevels.put(type, getUpgradeLevel(type) + 1);
            saveData();

            // Track challenge progress
            ChallengeManager.getInstance().onUpgradePurchased();

            return true;
        }
        return false;
    }

    /**
     * Get the actual multiplier/bonus value for an upgrade at current level
     */
    public double getUpgradeValue(UpgradeType type) {
        int level = getUpgradeLevel(type);
        return type.calculateValue(level);
    }

    /**
     * Apply upgrades to game entities based on current upgrade levels
     */
    public void applyUpgrades(Game game) {
        // Upgrades are applied dynamically when ships/planets are created
        // This method serves as a hook for any global upgrade applications if needed
        // Individual upgrades are applied via the getter methods when entities are
        // created
    }

    /**
     * Get upgrade multiplier (1.0 + bonus percentage / 100)
     */
    public double getUpgradeMultiplier(UpgradeType type) {
        return 1.0 + (getUpgradeValue(type) / 100.0);
    }

    /**
     * Get upgrade percentage value (for display and application)
     */
    public double getUpgradePercentage(UpgradeType type) {
        return getUpgradeValue(type);
    }

    // Ability-related methods
    public boolean isAbilityUnlocked(AbilityType type) {
        Boolean unlocked = abilitiesUnlocked.get(type);
        return unlocked != null && unlocked;
    }

    public int getAbilityLevel(AbilityType type) {
        return abilityLevels.get(type);
    }

    public int getAbilityCost(AbilityType type) {
        if (!abilitiesUnlocked.get(type)) {
            // Base unlock costs
            return type.getBaseCost();
        } else {
            // Upgrade costs
            int currentLevel = abilityLevels.get(type);
            if (currentLevel >= 20)
                return -1; // Max level reached
            return type.getUpgradeCost(currentLevel);
        }
    }

    public boolean purchaseAbility(AbilityType type) {
        int cost = getAbilityCost(type);
        if (cost > 0 && spendCoins(cost)) {
            boolean wasUnlocked = abilitiesUnlocked.get(type);
            if (!wasUnlocked) {
                abilitiesUnlocked.put(type, true);
                // Track ability unlock for challenges
                ChallengeManager.getInstance().onAbilityUnlocked();
                ChallengeManager.getInstance().onSpecificAbilityPurchased(type);
            } else {
                abilityLevels.put(type, abilityLevels.get(type) + 1);
            }
            saveData();
            return true;
        }
        return false;
    }

    public double getAbilityDuration(AbilityType type) {
        if (!isAbilityUnlocked(type))
            return 0;
        return type.calculateDuration(getAbilityLevel(type));
    }

    public int getAbilityPower(AbilityType type) {
        if (!isAbilityUnlocked(type))
            return 0;
        return type.calculatePower(getAbilityLevel(type));
    }

    // Save/Load functionality
    private void saveData() {
        SaveLoadManager.getInstance().savePlayerData(coins, achievementScore, bestTimes,
                upgradeLevels, abilitiesUnlocked, abilityLevels);
    }

    private void loadData() {
        PlayerDataContainer data = SaveLoadManager.getInstance().loadPlayerData();

        this.coins = data.coins;
        this.achievementScore = data.achievementScore;
        this.bestTimes = data.bestTimes;
        this.upgradeLevels = data.upgradeLevels;
        this.abilitiesUnlocked = data.abilitiesUnlocked;
        this.abilityLevels = data.abilityLevels;

        // Verify and fix achievement score if needed
        verifyAchievementScore();
    }

    /**
     * Verifies that the achievement score matches the sum of completed challenges.
     * If there's a mismatch, corrects the achievement score to the proper value.
     * This method should be called after loading data to ensure consistency.
     */
    private void verifyAchievementScore() {
        // Calculate the correct achievement score based on completed challenges
        int correctScore = calculateCorrectAchievementScore();

        // If the stored score doesn't match the calculated score, fix it
        if (this.achievementScore != correctScore) {
            System.out.println("Achievement score mismatch detected. Stored: " + this.achievementScore +
                    ", Calculated: " + correctScore + ". Correcting...");
            this.achievementScore = correctScore;
            saveData(); // Save the corrected score
        }
    }

    /**
     * Calculates what the achievement score should be based on completed
     * challenges.
     * This method gets the ChallengeManager instance and sums up the score values
     * of all completed challenges.
     * 
     * @return The correct achievement score based on completed challenges
     */
    private int calculateCorrectAchievementScore() {
        try {
            ChallengeManager challengeManager = ChallengeManager.getInstance();
            int totalScore = 0;

            // Sum up the score values of all completed challenges
            for (home.game.challenges.Challenge challenge : challengeManager.getAllChallenges()) {
                if (challenge.isCompleted()) {
                    totalScore += challenge.getRarity().getScoreValue();
                }
            }

            return totalScore;
        } catch (Exception e) {
            // If there's any error accessing ChallengeManager, return current score
            // This prevents issues during initialization or if ChallengeManager isn't ready
            System.err.println("Warning: Could not verify achievement score due to error: " + e.getMessage());
            return this.achievementScore;
        }
    }

}