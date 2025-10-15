package home.game.challenges;

import home.game.GameConstants;

public class ProgressNotification {
    private String challengeName;
    private int currentProgress;
    private int targetProgress;
    private float progressPercentage;
    private ChallengeRarity rarity;
    private long displayTime;

    public ProgressNotification(String challengeName, int currentProgress, int targetProgress,
            ChallengeRarity rarity) {
        this.challengeName = challengeName;
        this.currentProgress = currentProgress;
        this.targetProgress = targetProgress;
        this.progressPercentage = targetProgress > 0 ? (float) currentProgress / targetProgress : 0;
        this.rarity = rarity;
        this.displayTime = System.currentTimeMillis() + GameConstants.getProgressNotificationDuration(); // Show for
                                                                                                         // configured
                                                                                                         // duration
    }

    public String getChallengeName() {
        return challengeName;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getTargetProgress() {
        return targetProgress;
    }

    public float getProgressPercentage() {
        return progressPercentage;
    }

    public ChallengeRarity getRarity() {
        return rarity;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > displayTime;
    }
}
