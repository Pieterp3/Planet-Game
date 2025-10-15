package home.game.challenges;

import home.game.GameConstants;

public class AchievementNotification {
    private String challengeName;
    private ChallengeRarity rarity;
    private int coinReward;
    private int scoreReward;
    private long displayTime;

    public AchievementNotification(String challengeName, ChallengeRarity rarity,
            int coinReward, int scoreReward) {
        this.challengeName = challengeName;
        this.rarity = rarity;
        this.coinReward = coinReward;
        this.scoreReward = scoreReward;
        this.displayTime = System.currentTimeMillis() + GameConstants.getNotificationDisplayDuration(); // Show for
                                                                                                        // configured
                                                                                                        // duration
    }

    public String getChallengeName() {
        return challengeName;
    }

    public ChallengeRarity getRarity() {
        return rarity;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public int getScoreReward() {
        return scoreReward;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > displayTime;
    }
}
