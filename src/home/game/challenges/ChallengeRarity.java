package home.game.challenges;

public enum ChallengeRarity {
    COMMON(5, new java.awt.Color(169, 169, 169)), // Gray
    UNCOMMON(10, new java.awt.Color(30, 255, 0)), // Green
    RARE(25, new java.awt.Color(0, 112, 255)), // Blue
    EPIC(50, new java.awt.Color(163, 53, 238)), // Purple
    LEGENDARY(100, new java.awt.Color(255, 128, 0)); // Orange

    private final int scoreValue;
    private final java.awt.Color color;

    ChallengeRarity(int scoreValue, java.awt.Color color) {
        this.scoreValue = scoreValue;
        this.color = color;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public java.awt.Color getColor() {
        return color;
    }
}