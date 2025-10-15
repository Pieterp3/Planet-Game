package home.game.challenges;

public enum ChallengeType {
    // Time-based challenges
    COMPLETE_MISSION_TIME,

    // Ability-based challenges
    USE_ABILITIES_COUNT,
    USE_SPECIFIC_ABILITY,

    // Planet-based challenges
    CAPTURE_PLANETS,
    WIN_WITHOUT_LOSING_PLANET,
    WIN_WITHOUT_CAPTURING_PLANET_TYPE,

    // Progression challenges
    DONATE_GOLD,
    UNLOCK_ABILITIES,
    PURCHASE_SPECIFIC_ABILITY,
    PURCHASE_ABILITY_UPGRADES,
    PURCHASE_UPGRADES,

    // Streak challenges
    WIN_STREAK_DIFFICULTY
}