package home.game.challenges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import home.game.abilities.AbilityType;
import home.game.GameConstants;
import home.game.io.SaveLoadManager;
import home.game.io.datacontainers.ChallengeData;
import home.game.operators.Difficulty;
import home.game.operators.player.PlayerData;
import home.game.operators.player.UpgradeType;
import home.game.planets.PlanetType;

public class ChallengeManager {
    private static ChallengeManager instance;
    private Map<String, Challenge> challenges;
    private List<String> activeChallenges; // Currently tracking challenges
    private PlayerData playerData;

    // Game session tracking
    private long gameStartTime;
    private Map<AbilityType, Integer> specificAbilityUsage;
    private boolean planetsLostThisGame;
    private Set<PlanetType> planetTypesCapturedThisGame;
    private Difficulty currentGameDifficulty;

    // Career/total tracking (persistent across games)
    private int totalPlanetsCaptured = 0;
    private int totalAbilitiesUsed = 0;
    private int totalGoldDonated = 0;

    // Save management
    private boolean pendingSave = false;
    private long lastSaveRequest = 0;

    // Achievement notifications
    private Queue<AchievementNotification> pendingNotifications;
    private Queue<ProgressNotification> pendingProgressNotifications;

    private ChallengeManager() {
        challenges = new HashMap<>();
        activeChallenges = new ArrayList<>();
        specificAbilityUsage = new HashMap<>();
        planetTypesCapturedThisGame = new HashSet<>();
        pendingNotifications = new LinkedList<>();
        pendingProgressNotifications = new LinkedList<>();
        playerData = PlayerData.getInstance();
        initializeChallenges();
        loadChallengeData(); // Load saved challenge progress
        checkForAlreadyUnlockedAbilities(); // Failsafe for pre-existing unlocked abilities
        updateProgressBasedOnCareerStats(); // Update challenges based on loaded career stats
    }

    public static ChallengeManager getInstance() {
        if (instance == null) {
            instance = new ChallengeManager();
        }
        return instance;
    }

    private void initializeChallenges() {
        // Time-based challenges
        Challenge speedEasy = addChallenge("speed_easy", "Speed Demon I",
                "Complete an Easy mission in under 60 seconds",
                ChallengeType.COMPLETE_MISSION_TIME, ChallengeRarity.COMMON, 50);
        speedEasy.setRequiredDifficulty(Difficulty.EASY);
        speedEasy.setTimeLimit(60000);

        Challenge speedMedium = addChallenge("speed_medium", "Speed Demon II",
                "Complete a Medium mission in under 90 seconds",
                ChallengeType.COMPLETE_MISSION_TIME, ChallengeRarity.UNCOMMON, 100);
        speedMedium.setRequiredDifficulty(Difficulty.MEDIUM);
        speedMedium.setTimeLimit(90000);

        Challenge speedHard = addChallenge("speed_hard", "Lightning Strike",
                "Complete a Hard mission in under 120 seconds",
                ChallengeType.COMPLETE_MISSION_TIME, ChallengeRarity.RARE, 200);
        speedHard.setRequiredDifficulty(Difficulty.HARD);
        speedHard.setTimeLimit(120000);

        Challenge speedExtreme = addChallenge("speed_extreme", "Impossible Speed",
                "Complete an Extreme mission in under 180 seconds",
                ChallengeType.COMPLETE_MISSION_TIME, ChallengeRarity.LEGENDARY, 500);
        speedExtreme.setRequiredDifficulty(Difficulty.EXTREME);
        speedExtreme.setTimeLimit(180000);

        // Ability usage challenges
        Challenge abilityNovice = addChallenge("ability_novice", "Ability Novice", "Use 25 abilities in total",
                ChallengeType.USE_ABILITIES_COUNT, ChallengeRarity.COMMON, 30);
        abilityNovice.setTargetCount(25);

        Challenge abilityAdept = addChallenge("ability_adept", "Ability Adept", "Use 100 abilities in total",
                ChallengeType.USE_ABILITIES_COUNT, ChallengeRarity.UNCOMMON, 75);
        abilityAdept.setTargetCount(100);

        Challenge abilityMaster = addChallenge("ability_master", "Ability Master", "Use 500 abilities in total",
                ChallengeType.USE_ABILITIES_COUNT, ChallengeRarity.RARE, 200);
        abilityMaster.setTargetCount(500);

        // Specific ability challenges
        for (AbilityType ability : AbilityType.values()) {
            String abilityName = ability.name().toLowerCase().replace("_", " ");
            abilityName = Character.toUpperCase(abilityName.charAt(0)) + abilityName.substring(1);

            Challenge abilitySpecialist = addChallenge("ability_" + ability.name().toLowerCase() + "_specialist",
                    abilityName + " Specialist", "Use " + abilityName + " 20 times",
                    ChallengeType.USE_SPECIFIC_ABILITY, ChallengeRarity.UNCOMMON, 80);
            abilitySpecialist.setSpecificAbility(ability);
            abilitySpecialist.setTargetCount(20);
        }

        // Individual ability purchase challenges
        for (AbilityType ability : AbilityType.values()) {
            String abilityName = ability.getDisplayName();

            Challenge abilityPurchaser = addChallenge("purchase_" + ability.name().toLowerCase(),
                    "Unlock " + abilityName, "Purchase the " + abilityName + " ability",
                    ChallengeType.PURCHASE_SPECIFIC_ABILITY, ChallengeRarity.COMMON, 50);
            abilityPurchaser.setSpecificAbility(ability);
            abilityPurchaser.setTargetCount(1);
        }

        // Planet capture challenges
        Challenge conquerorBasic = addChallenge("conqueror_basic", "Basic Conqueror", "Capture 50 planets total",
                ChallengeType.CAPTURE_PLANETS, ChallengeRarity.COMMON, 40);
        conquerorBasic.setTargetCount(50);

        Challenge conquerorAdvanced = addChallenge("conqueror_advanced", "Advanced Conqueror",
                "Capture 200 planets total",
                ChallengeType.CAPTURE_PLANETS, ChallengeRarity.UNCOMMON, 100);
        conquerorAdvanced.setTargetCount(200);

        Challenge conquerorMaster = addChallenge("conqueror_master", "Galactic Emperor", "Capture 1000 planets total",
                ChallengeType.CAPTURE_PLANETS, ChallengeRarity.EPIC, 300);
        conquerorMaster.setTargetCount(1000);

        // Perfect game challenges
        Challenge perfectEasy = addChallenge("perfect_easy", "Easy Perfectionist",
                "Win 5 Easy games without losing a planet",
                ChallengeType.WIN_WITHOUT_LOSING_PLANET, ChallengeRarity.UNCOMMON, 80);
        perfectEasy.setRequiredDifficulty(Difficulty.EASY);
        perfectEasy.setTargetCount(5);

        Challenge perfectMedium = addChallenge("perfect_medium", "Medium Perfectionist",
                "Win 3 Medium games without losing a planet",
                ChallengeType.WIN_WITHOUT_LOSING_PLANET, ChallengeRarity.RARE, 150);
        perfectMedium.setRequiredDifficulty(Difficulty.MEDIUM);
        perfectMedium.setTargetCount(3);

        Challenge perfectHard = addChallenge("perfect_hard", "Untouchable", "Win 1 Hard game without losing a planet",
                ChallengeType.WIN_WITHOUT_LOSING_PLANET, ChallengeRarity.EPIC, 300);
        perfectHard.setRequiredDifficulty(Difficulty.HARD);
        perfectHard.setTargetCount(1);

        // Gold donation challenges
        Challenge generousBasic = addChallenge("generous_basic", "Generous Soul", "Donate 500 gold total", // We'll add
                                                                                                           // donation
                                                                                                           // feature
                ChallengeType.DONATE_GOLD, ChallengeRarity.COMMON, 25);
        generousBasic.setTargetCount(500);

        Challenge generousAdvanced = addChallenge("generous_advanced", "Philanthropist", "Donate 2000 gold total",
                ChallengeType.DONATE_GOLD, ChallengeRarity.RARE, 100);
        generousAdvanced.setTargetCount(2000);

        // Progression challenges
        Challenge unlockCollector = addChallenge("unlock_collector", "Ability Collector",
                "Unlock 5 different abilities",
                ChallengeType.UNLOCK_ABILITIES, ChallengeRarity.UNCOMMON, 60);
        unlockCollector.setTargetCount(5);

        Challenge upgradeBuyer = addChallenge("upgrade_buyer", "Upgrade Enthusiast", "Purchase 50 upgrades total",
                ChallengeType.PURCHASE_UPGRADES, ChallengeRarity.UNCOMMON, 75);
        upgradeBuyer.setTargetCount(50);

        // Planet type restriction challenges
        Challenge noAttackPlanets = addChallenge("no_attack_planets", "Pacifist Victory",
                "Win a game without capturing Attack planets",
                ChallengeType.WIN_WITHOUT_CAPTURING_PLANET_TYPE, ChallengeRarity.RARE, 120);
        noAttackPlanets.setExcludedPlanetType(PlanetType.ATTACK);

        Challenge noDefensePlanets = addChallenge("no_defense_planets", "Offensive Master",
                "Win a game without capturing Defense planets",
                ChallengeType.WIN_WITHOUT_CAPTURING_PLANET_TYPE, ChallengeRarity.RARE, 120);
        noDefensePlanets.setExcludedPlanetType(PlanetType.DEFENCE);

        Challenge noSpeedPlanets = addChallenge("no_speed_planets", "Methodical Victory",
                "Win a game without capturing Speed planets",
                ChallengeType.WIN_WITHOUT_CAPTURING_PLANET_TYPE, ChallengeRarity.RARE, 120);
        noSpeedPlanets.setExcludedPlanetType(PlanetType.SPEED);
    }

    private Challenge addChallenge(String id, String name, String description,
            ChallengeType type, ChallengeRarity rarity, int coinReward) {
        Challenge challenge = new Challenge(id, name, description, type, rarity, coinReward);
        challenges.put(id, challenge);
        if (!activeChallenges.contains(id)) {
            activeChallenges.add(id);
        }
        return challenge;
    }

    // Game session management
    public void onNewGame(Difficulty difficulty) {
        currentGameDifficulty = difficulty;
        gameStartTime = System.currentTimeMillis();
        // Don't clear specificAbilityUsage - it should persist across games for career
        // challenges
        planetsLostThisGame = false;
        planetTypesCapturedThisGame.clear();
    }

    public void onAbilityUsed(AbilityType ability) {
        totalAbilitiesUsed++;
        specificAbilityUsage.put(ability, specificAbilityUsage.getOrDefault(ability, 0) + 1);

        // Save progress after updating specific ability count
        saveProgressChange();

        // Check ability-related challenges (using total for career challenges)
        checkProgressAndComplete("ability_novice", totalAbilitiesUsed);
        checkProgressAndComplete("ability_adept", totalAbilitiesUsed);
        checkProgressAndComplete("ability_master", totalAbilitiesUsed);

        // Check specific ability challenges
        int specificCount = specificAbilityUsage.get(ability);
        String challengeId = "ability_" + ability.name().toLowerCase() + "_specialist";
        checkProgressAndComplete(challengeId, specificCount);
    }

    public void onPlanetCaptured(PlanetType planetType) {
        totalPlanetsCaptured++;
        planetTypesCapturedThisGame.add(planetType);

        // Check planet capture challenges (using total for career challenges)
        checkProgressAndComplete("conqueror_basic", totalPlanetsCaptured);
        checkProgressAndComplete("conqueror_advanced", totalPlanetsCaptured);
        checkProgressAndComplete("conqueror_master", totalPlanetsCaptured);
    }

    public void onPlanetLost() {
        planetsLostThisGame = true;
    }

    public void onGameWon() {
        long gameDuration = System.currentTimeMillis() - gameStartTime;

        // Check time-based challenges
        checkTimeChallenges(gameDuration);

        // Check perfect game challenges
        if (!planetsLostThisGame) {
            checkPerfectGameChallenges();
        }

        // Check planet type restriction challenges
        checkPlanetTypeRestrictionChallenges();
    }

    private void checkTimeChallenges(long gameDuration) {
        for (Challenge challenge : getActiveChallenges()) {
            if (challenge.getType() == ChallengeType.COMPLETE_MISSION_TIME &&
                    challenge.getRequiredDifficulty() == currentGameDifficulty &&
                    gameDuration <= challenge.getTimeLimit() &&
                    !challenge.isCompleted()) {

                completeChallenge(challenge.getId());
            }
        }
    }

    private void checkPerfectGameChallenges() {
        for (Challenge challenge : getActiveChallenges()) {
            if (challenge.getType() == ChallengeType.WIN_WITHOUT_LOSING_PLANET) {
                if (challenge.getRequiredDifficulty() == currentGameDifficulty && !challenge.isCompleted()) {
                    // Increment progress and check if complete
                    challenge.incrementProgress();
                    saveProgressChange(); // Save progress immediately
                    if (challenge.getCurrentProgress() >= challenge.getTargetCount()) {
                        completeChallenge(challenge.getId());
                    }
                }
            }
        }
    }

    private void checkPlanetTypeRestrictionChallenges() {
        for (Challenge challenge : getActiveChallenges()) {
            if (challenge.getType() == ChallengeType.WIN_WITHOUT_CAPTURING_PLANET_TYPE &&
                    !challenge.isCompleted()) {

                if (!planetTypesCapturedThisGame.contains(challenge.getExcludedPlanetType())) {
                    completeChallenge(challenge.getId());
                }
            }
        }
    }

    private void checkProgressAndComplete(String challengeId, int currentProgress) {
        Challenge challenge = challenges.get(challengeId);
        if (challenge != null && !challenge.isCompleted()) {
            int previousProgress = challenge.getCurrentProgress();
            challenge.setCurrentProgress(currentProgress);
            saveProgressChange(); // Save progress immediately

            // Show progress notification if progress was made and we're in a game session
            if (currentProgress > previousProgress && currentProgress > 0 && gameStartTime > 0) {
                pendingProgressNotifications.offer(new ProgressNotification(
                        challenge.getName(),
                        currentProgress,
                        challenge.getTargetCount(),
                        challenge.getRarity()));
            }

            if (challenge.checkCompletion()) {
                completeChallenge(challengeId);
            }
        }
    }

    public void onUpgradePurchased() {
        int totalUpgrades = 0;
        for (UpgradeType type : UpgradeType.values()) {
            totalUpgrades += playerData.getUpgradeLevel(type);
        }
        checkProgressAndComplete("upgrade_buyer", totalUpgrades);
    }

    public void onAbilityUnlocked() {
        int unlockedCount = 0;
        for (AbilityType type : AbilityType.values()) {
            if (playerData.isAbilityUnlocked(type)) {
                unlockedCount++;
            }
        }
        checkProgressAndComplete("unlock_collector", unlockedCount);
    }

    public void onSpecificAbilityPurchased(AbilityType abilityType) {
        String challengeId = "purchase_" + abilityType.name().toLowerCase();
        Challenge challenge = challenges.get(challengeId);
        if (challenge != null && !challenge.isCompleted()) {
            challenge.incrementProgress();
            saveProgressChange(); // Save progress immediately
            if (challenge.checkCompletion()) {
                completeChallenge(challengeId);
            }
        }
    }

    public void onGoldDonated(int amount) {
        totalGoldDonated += amount;

        Challenge basicChallenge = challenges.get("generous_basic");
        Challenge advancedChallenge = challenges.get("generous_advanced");

        if (basicChallenge != null && !basicChallenge.isCompleted()) {
            basicChallenge.setCurrentProgress(totalGoldDonated);
            saveProgressChange(); // Save progress immediately
            if (basicChallenge.checkCompletion()) {
                completeChallenge("generous_basic");
            }
        }

        if (advancedChallenge != null && !advancedChallenge.isCompleted()) {
            advancedChallenge.setCurrentProgress(totalGoldDonated);
            saveProgressChange(); // Save progress immediately
            if (advancedChallenge.checkCompletion()) {
                completeChallenge("generous_advanced");
            }
        }
    }

    private void completeChallenge(String challengeId) {
        Challenge challenge = challenges.get(challengeId);
        if (challenge != null && !challenge.isCompleted()) {
            challenge.setCompleted(true);
            saveProgressChange(); // Save completion status immediately

            // Award rewards
            playerData.addCoins(challenge.getCoinReward());
            playerData.addAchievementScore(challenge.getRarity().getScoreValue());

            // Add notification
            pendingNotifications.offer(new AchievementNotification(
                    challenge.getName(),
                    challenge.getRarity(),
                    challenge.getCoinReward(),
                    challenge.getRarity().getScoreValue()));
        }
    }

    // Getters
    public List<Challenge> getAllChallenges() {
        return new ArrayList<>(challenges.values());
    }

    public List<Challenge> getActiveChallenges() {
        List<Challenge> active = new ArrayList<>();
        for (String id : activeChallenges) {
            Challenge challenge = challenges.get(id);
            if (challenge != null && !challenge.isCompleted()) {
                active.add(challenge);
            }
        }
        return active;
    }

    public List<Challenge> getCompletedChallenges() {
        List<Challenge> completed = new ArrayList<>();
        for (Challenge challenge : challenges.values()) {
            if (challenge.isCompleted()) {
                completed.add(challenge);
            }
        }
        return completed;
    }

    public Challenge getChallenge(String id) {
        return challenges.get(id);
    }

    public Queue<AchievementNotification> getPendingNotifications() {
        return pendingNotifications;
    }

    public Queue<ProgressNotification> getPendingProgressNotifications() {
        return pendingProgressNotifications;
    }

    public AchievementNotification getNextNotification() {
        // Remove expired notifications
        while (!pendingNotifications.isEmpty() && pendingNotifications.peek().isExpired()) {
            pendingNotifications.poll();
        }
        return pendingNotifications.peek();
    }

    public void clearNotification() {
        if (!pendingNotifications.isEmpty()) {
            pendingNotifications.poll();
        }
    }

    public ProgressNotification getNextProgressNotification() {
        // Remove expired notifications
        while (!pendingProgressNotifications.isEmpty() && pendingProgressNotifications.peek().isExpired()) {
            pendingProgressNotifications.poll();
        }
        return pendingProgressNotifications.peek();
    }

    public void clearProgressNotification() {
        if (!pendingProgressNotifications.isEmpty()) {
            pendingProgressNotifications.poll();
        }
    }

    // Save/Load functionality
    private void saveChallengeData() {
        SaveLoadManager.getInstance().saveChallengeData(challenges, totalPlanetsCaptured,
                totalAbilitiesUsed, totalGoldDonated,
                specificAbilityUsage);
    }

    private void loadChallengeData() {
        ChallengeData data = SaveLoadManager.getInstance().loadChallengeData(challenges);

        this.totalPlanetsCaptured = data.totalPlanetsCaptured;
        this.totalAbilitiesUsed = data.totalAbilitiesUsed;
        this.totalGoldDonated = data.totalGoldDonated;
        this.specificAbilityUsage = data.specificAbilityUsage;
    }

    // Call this method whenever challenge progress changes
    private void saveProgressChange() {
        pendingSave = true;
        lastSaveRequest = System.currentTimeMillis();
    }

    // Call this method periodically to handle delayed saves
    public void updateSaveState() {
        if (pendingSave && System.currentTimeMillis() - lastSaveRequest >= GameConstants.getChallengeSaveDelay()) {
            saveChallengeData();
            pendingSave = false;
        }
    }

    // Force immediate save (e.g., when game is closing)
    public void forceSave() {
        if (pendingSave) {
            saveChallengeData();
            pendingSave = false;
        }
    }

    /**
     * Failsafe method to check for already-unlocked abilities and mark their
     * purchase achievements as complete
     */
    private void checkForAlreadyUnlockedAbilities() {
        for (AbilityType ability : AbilityType.values()) {
            if (playerData.isAbilityUnlocked(ability)) {
                String challengeId = "purchase_" + ability.name().toLowerCase();
                Challenge challenge = challenges.get(challengeId);

                if (challenge != null && !challenge.isCompleted()) {
                    challenge.incrementProgress();
                    saveProgressChange(); // Save the completion
                    if (challenge.checkCompletion()) {
                        completeChallenge(challengeId);
                    }
                }
            }
        }
    }

    /**
     * Updates challenge progress based on loaded career statistics
     */
    private void updateProgressBasedOnCareerStats() {
        // Update planet capture challenges if we have accumulated stats
        if (totalPlanetsCaptured > 0) {
            checkProgressAndComplete("conqueror_basic", totalPlanetsCaptured);
            checkProgressAndComplete("conqueror_advanced", totalPlanetsCaptured);
            checkProgressAndComplete("conqueror_master", totalPlanetsCaptured);
        }

        // Update ability usage challenges if we have accumulated stats
        if (totalAbilitiesUsed > 0) {
            checkProgressAndComplete("ability_novice", totalAbilitiesUsed);
            checkProgressAndComplete("ability_adept", totalAbilitiesUsed);
            checkProgressAndComplete("ability_master", totalAbilitiesUsed);
        }

        // Update specific ability challenges based on career stats
        for (AbilityType ability : specificAbilityUsage.keySet()) {
            int count = specificAbilityUsage.get(ability);
            if (count > 0) {
                String challengeId = "ability_" + ability.name().toLowerCase() + "_specialist";
                checkProgressAndComplete(challengeId, count);
            }
        }
    }
}