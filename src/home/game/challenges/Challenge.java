package home.game.challenges;

import home.game.abilities.AbilityType;
import home.game.operators.Difficulty;
import home.game.planets.PlanetType;

public class Challenge {

    private String id;
    private String name;
    private String description;
    private ChallengeType type;
    private ChallengeRarity rarity;
    private int coinReward;
    private boolean completed;

    // Challenge parameters
    private Difficulty requiredDifficulty;
    private long timeLimit; // in milliseconds
    private int targetCount;
    private AbilityType specificAbility;
    private PlanetType excludedPlanetType;

    // Progress tracking
    private int currentProgress;
    private long startTime;

    public Challenge(String id, String name, String description, ChallengeType type,
            ChallengeRarity rarity, int coinReward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rarity = rarity;
        this.coinReward = coinReward;
        this.completed = false;
        this.currentProgress = 0;
        this.startTime = 0;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ChallengeType getType() {
        return type;
    }

    public ChallengeRarity getRarity() {
        return rarity;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Parameter getters and setters
    public Difficulty getRequiredDifficulty() {
        return requiredDifficulty;
    }

    public void setRequiredDifficulty(Difficulty difficulty) {
        this.requiredDifficulty = difficulty;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public AbilityType getSpecificAbility() {
        return specificAbility;
    }

    public void setSpecificAbility(AbilityType ability) {
        this.specificAbility = ability;
    }

    public PlanetType getExcludedPlanetType() {
        return excludedPlanetType;
    }

    public void setExcludedPlanetType(PlanetType planetType) {
        this.excludedPlanetType = planetType;
    }

    // Progress tracking
    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
    }

    public void incrementProgress() {
        this.currentProgress++;
    }

    public void incrementProgress(int amount) {
        this.currentProgress += amount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public float getProgressPercentage() {
        if (targetCount == 0)
            return completed ? 1.0f : 0.0f;
        return Math.min(1.0f, (float) currentProgress / targetCount);
    }

    public boolean checkCompletion() {
        switch (type) {
            case USE_ABILITIES_COUNT:
            case USE_SPECIFIC_ABILITY:
            case CAPTURE_PLANETS:
            case DONATE_GOLD:
            case UNLOCK_ABILITIES:
            case PURCHASE_SPECIFIC_ABILITY:
            case PURCHASE_ABILITY_UPGRADES:
            case PURCHASE_UPGRADES:
            case WIN_STREAK_DIFFICULTY:
                return currentProgress >= targetCount;

            case COMPLETE_MISSION_TIME:
                // This is checked externally when mission completes
                return completed;

            case WIN_WITHOUT_LOSING_PLANET:
                return currentProgress >= targetCount;

            case WIN_WITHOUT_CAPTURING_PLANET_TYPE:
                // These are checked externally on game win
                return completed;

            default:
                return false;
        }
    }

    public void reset() {
        this.currentProgress = 0;
        this.startTime = 0;
        this.completed = false;
    }
}