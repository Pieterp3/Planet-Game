package home.game.io;

import java.awt.Color;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import home.game.GameConstants;
import home.game.abilities.AbilityType;
import home.game.challenges.Challenge;
import home.game.io.datacontainers.ChallengeData;
import home.game.io.datacontainers.PlayerDataContainer;
import home.game.io.datacontainers.VisualSettingsContainer;
import home.game.operators.Difficulty;
import home.game.operators.player.UpgradeType;

/**
 * Centralized save/load manager for all game data persistence.
 * All files are stored in the cache directory: {user.home}/SpaceGameCache/
 * Handles four different save files:
 * 1. challenge_data.dat - Challenge progress and statistics
 * 2. game_constants.properties - Game configuration settings
 * 3. player_data.dat - Player progression and upgrades
 * 4. visual_settings.dat - Display preferences
 */
public class SaveLoadManager {

    // File constants
    public static final String CACHE_LOCATION = System.getProperty("user.home") + File.separator + "SpaceGameCache"
            + File.separator;
    public static final String CHALLENGE_DATA_FILE = "challenge_data.dat";
    public static final String GAME_CONSTANTS_FILE = "game_constants.properties";
    public static final String PLAYER_DATA_FILE = "player_data.dat";
    public static final String VISUAL_SETTINGS_FILE = "visual_settings.dat";

    private static SaveLoadManager instance;

    private SaveLoadManager() {
        // Private constructor for singleton
    }

    public static SaveLoadManager getInstance() {
        if (instance == null) {
            instance = new SaveLoadManager();
        }
        return instance;
    }

    /**
     * Get the full path to a cache file, ensuring the cache directory exists
     */
    private String getCacheFilePath(String filename) {
        File cacheDir = new File(CACHE_LOCATION);
        if (!cacheDir.exists()) {
            if (cacheDir.mkdirs()) {
                System.out.println("Created cache directory: " + CACHE_LOCATION);
            } else {
                System.err.println("Failed to create cache directory: " + CACHE_LOCATION);
            }
        }
        return CACHE_LOCATION + filename;
    }

    // ===========================================
    // CHALLENGE DATA OPERATIONS
    // ===========================================

    /**
     * Save challenge data including progress, completion status, and career
     * statistics
     */
    public void saveChallengeData(Map<String, Challenge> challenges,
            int totalPlanetsCaptured,
            int totalAbilitiesUsed,
            int totalGoldDonated,
            Map<AbilityType, Integer> specificAbilityUsage) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(getCacheFilePath(CHALLENGE_DATA_FILE)))) {
            // Save challenge progress and completion status
            Map<String, Integer> challengeProgress = new HashMap<>();
            Map<String, Boolean> challengeCompletion = new HashMap<>();

            for (Map.Entry<String, Challenge> entry : challenges.entrySet()) {
                challengeProgress.put(entry.getKey(), entry.getValue().getCurrentProgress());
                challengeCompletion.put(entry.getKey(), entry.getValue().isCompleted());
            }

            oos.writeObject(challengeProgress);
            oos.writeObject(challengeCompletion);
            oos.writeInt(totalPlanetsCaptured);
            oos.writeInt(totalAbilitiesUsed);
            oos.writeInt(totalGoldDonated);
            oos.writeObject(specificAbilityUsage);

            System.out.println("Challenge data saved successfully");
        } catch (IOException e) {
            System.err.println("Failed to save challenge data: " + e.getMessage());
        }
    }

    /**
     * Load challenge data and apply it to the challenges map
     * Returns a ChallengeData object containing all loaded data
     */
    @SuppressWarnings("unchecked")
    public ChallengeData loadChallengeData(Map<String, Challenge> challenges) {
        File file = new File(getCacheFilePath(CHALLENGE_DATA_FILE));
        if (!file.exists()) {
            return new ChallengeData(); // Return default values
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(getCacheFilePath(CHALLENGE_DATA_FILE)))) {
            Map<String, Integer> challengeProgress = (Map<String, Integer>) ois.readObject();
            Map<String, Boolean> challengeCompletion = (Map<String, Boolean>) ois.readObject();

            // Apply loaded data to challenges
            for (Map.Entry<String, Integer> entry : challengeProgress.entrySet()) {
                Challenge challenge = challenges.get(entry.getKey());
                if (challenge != null) {
                    challenge.setCurrentProgress(entry.getValue());
                }
            }
            for (Map.Entry<String, Boolean> entry : challengeCompletion.entrySet()) {
                Challenge challenge = challenges.get(entry.getKey());
                if (challenge != null) {
                    challenge.setCompleted(entry.getValue());
                }
            }

            // Load career statistics (with backwards compatibility)
            int totalPlanetsCaptured = 0;
            int totalAbilitiesUsed = 0;
            int totalGoldDonated = 0;
            Map<AbilityType, Integer> specificAbilityUsage = new HashMap<>();

            try {
                totalPlanetsCaptured = ois.readInt();
            } catch (EOFException e) {
                // Very old save file format, no career stats
            }
            try {
                totalAbilitiesUsed = ois.readInt();
            } catch (EOFException e) {
                // Older save file format, no ability stats yet
            }
            try {
                totalGoldDonated = ois.readInt();
            } catch (EOFException e) {
                // Older save file format, no gold stats yet
            }
            try {
                specificAbilityUsage = (Map<AbilityType, Integer>) ois.readObject();
                if (specificAbilityUsage == null) {
                    specificAbilityUsage = new HashMap<>();
                }
            } catch (EOFException | ClassNotFoundException e) {
                // Older save file format, no specific ability stats yet
            }

            System.out.println("Challenge data loaded successfully");
            return new ChallengeData(totalPlanetsCaptured, totalAbilitiesUsed, totalGoldDonated, specificAbilityUsage);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load challenge data: " + e.getMessage());
            return new ChallengeData(); // Return default values
        }
    }

    // ===========================================
    // GAME CONSTANTS OPERATIONS
    // ===========================================

    /**
     * Load game constants from properties file using reflection
     */
    public void loadGameConstants() {
        Properties props = new Properties();
        File configFile = new File(getCacheFilePath(GAME_CONSTANTS_FILE));

        try {
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(getCacheFilePath(GAME_CONSTANTS_FILE))) {
                    props.load(fis);
                }
                boolean restoreDefaults = Boolean
                        .parseBoolean(props.getProperty("RESTORE_DEFAULTS", "false"));

                // Use reflection to set field values
                Field[] fields = GameConstants.class.getDeclaredFields();
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) &&
                            !Modifier.isFinal(field.getModifiers()) &&
                            !field.getName().equals("CONFIG_FILE")) {

                        String value = props.getProperty(field.getName());
                        if (value != null) {
                            setFieldValue(field, value);
                        }
                    }
                }
                if (restoreDefaults) {
                    GameConstants.resetToDefaults();
                    saveGameConstants(); // Save the restored defaults back to file
                }
                System.out.println("Configuration loaded from " + getCacheFilePath(GAME_CONSTANTS_FILE));
            } else {
                // Create default config file if it doesn't exist
                saveGameConstants();
                System.out.println("Created default configuration file: " + getCacheFilePath(GAME_CONSTANTS_FILE));
            }
        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save current game constants to properties file using reflection
     */
    public void saveGameConstants() {
        try {
            // Use reflection to get all static field values in declaration order
            Field[] fields = GameConstants.class.getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            sb.append("#Planet Game Configuration File\n");
            sb.append("#Edit values as needed. Changes will be loaded automatically on game start.\n");
            sb.append("#Format: VARIABLE_NAME=value\n");
            sb.append("#Supported types: int, double, long, boolean, String");

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) &&
                        !Modifier.isFinal(field.getModifiers()) &&
                        !field.getName().equals("CONFIG_FILE")) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (field.getName().contains("TITLE")) {
                        sb.append("\n\n#" + value.toString() + "");
                    } else if (field.getName().equals("RESTORE_DEFAULTS")) {
                        sb.append("\n" + field.getName()).append("=").append("false").append("");
                    } else {
                        sb.append("\n" + field.getName()).append("=").append(value.toString()).append("");
                    }
                }
            }

            // Write to file manually to preserve order
            try (FileOutputStream fos = new FileOutputStream(getCacheFilePath(GAME_CONSTANTS_FILE))) {
                fos.write(sb.toString().getBytes());
            }

            System.out.println("Configuration saved to " + getCacheFilePath(GAME_CONSTANTS_FILE));
        } catch (Exception e) {
            System.err.println("Error saving configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets a field value from a string representation using the appropriate type
     * conversion
     */
    private void setFieldValue(Field field, String value) throws IllegalAccessException {
        field.setAccessible(true);
        Class<?> type = field.getType();

        try {
            if (type == int.class) {
                field.setInt(null, Integer.parseInt(value));
            } else if (type == double.class) {
                field.setDouble(null, Double.parseDouble(value));
            } else if (type == long.class) {
                field.setLong(null, Long.parseLong(value));
            } else if (type == boolean.class) {
                field.setBoolean(null, Boolean.parseBoolean(value));
            } else if (type == String.class) {
                field.set(null, value);
            } else {
                System.err.println("Unsupported field type: " + type.getSimpleName() + " for field " + field.getName());
            }
        } catch (NumberFormatException e) {
            System.err.println(
                    "Invalid value '" + value + "' for field " + field.getName() + " of type " + type.getSimpleName());
        }
    }

    // ===========================================
    // PLAYER DATA OPERATIONS
    // ===========================================

    /**
     * Save player data including coins, upgrades, abilities, and best times
     */
    public void savePlayerData(int coins,
            int achievementScore,
            Map<Difficulty, Long> bestTimes,
            Map<UpgradeType, Integer> upgradeLevels,
            Map<AbilityType, Boolean> abilitiesUnlocked,
            Map<AbilityType, Integer> abilityLevels) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(getCacheFilePath(PLAYER_DATA_FILE)))) {
            oos.writeInt(coins);
            oos.writeInt(achievementScore);
            oos.writeObject(bestTimes);
            oos.writeObject(upgradeLevels);
            oos.writeObject(abilitiesUnlocked);
            oos.writeObject(abilityLevels);

            System.out.println("Player data saved successfully");
        } catch (IOException e) {
            System.err.println("Failed to save player data: " + e.getMessage());
        }
    }

    /**
     * Load player data and return a PlayerDataContainer with all the values
     */
    @SuppressWarnings("unchecked")
    public PlayerDataContainer loadPlayerData() {
        File file = new File(getCacheFilePath(PLAYER_DATA_FILE));
        if (!file.exists()) {
            return new PlayerDataContainer(); // Return default values
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getCacheFilePath(PLAYER_DATA_FILE)))) {
            int coins = ois.readInt();

            // Load achievement score (backwards compatibility)
            int achievementScore = 0;
            try {
                achievementScore = ois.readInt();
            } catch (Exception e) {
                // Older format without achievement score
            }

            Map<Difficulty, Long> bestTimes = (Map<Difficulty, Long>) ois.readObject();
            Map<UpgradeType, Integer> upgradeLevels = (Map<UpgradeType, Integer>) ois.readObject();

            // Load abilities data (backwards compatibility)
            Map<AbilityType, Boolean> abilitiesUnlocked = new HashMap<>();
            Map<AbilityType, Integer> abilityLevels = new HashMap<>();

            try {
                abilitiesUnlocked = (Map<AbilityType, Boolean>) ois.readObject();
                abilityLevels = (Map<AbilityType, Integer>) ois.readObject();
            } catch (Exception e) {
                // Initialize abilities if not found in save file
                for (AbilityType type : AbilityType.values()) {
                    abilitiesUnlocked.put(type, false);
                    abilityLevels.put(type, 0);
                }
            }

            // Ensure all upgrades are present (for when new upgrades are added)
            for (UpgradeType type : UpgradeType.values()) {
                upgradeLevels.putIfAbsent(type, 0);
            }

            // Ensure all abilities are present (for when new abilities are added)
            for (AbilityType type : AbilityType.values()) {
                abilitiesUnlocked.putIfAbsent(type, false);
                abilityLevels.putIfAbsent(type, 0);
            }

            System.out.println("Player data loaded successfully");
            return new PlayerDataContainer(coins, achievementScore, bestTimes, upgradeLevels, abilitiesUnlocked,
                    abilityLevels);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Player data format has changed, resetting to defaults. Reason: " + e.getMessage());
            return new PlayerDataContainer(); // Return default values
        }
    }

    // ===========================================
    // VISUAL SETTINGS OPERATIONS
    // ===========================================

    /**
     * Save visual settings to file
     */
    public void saveVisualSettings(boolean displayConnectionLines,
            boolean displayEffects,
            boolean displayProjectiles,
            boolean displayPlanetMoons,
            boolean displayShips,
            float connectionLineOpacity,
            Color playerPlanetColor,
            Map<Integer, AbilityType> keybindMap) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(getCacheFilePath(VISUAL_SETTINGS_FILE)))) {
            oos.writeBoolean(displayConnectionLines);
            oos.writeBoolean(displayEffects);
            oos.writeBoolean(displayProjectiles);
            oos.writeBoolean(displayPlanetMoons);
            oos.writeBoolean(displayShips);
            oos.writeFloat(connectionLineOpacity);
            oos.writeObject(playerPlanetColor);
            oos.writeObject(keybindMap);

            System.out.println("Visual settings saved successfully");
        } catch (IOException e) {
            System.err.println("Could not save visual settings: " + e.getMessage());
        }
    }

    /**
     * Load visual settings from file
     */
    @SuppressWarnings("unchecked")
    public VisualSettingsContainer loadVisualSettings() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(getCacheFilePath(VISUAL_SETTINGS_FILE)))) {
            boolean displayConnectionLines = ois.readBoolean();
            boolean displayEffects = ois.readBoolean();
            boolean displayProjectiles = ois.readBoolean();
            boolean displayPlanetMoons = ois.readBoolean();
            boolean displayShips = ois.readBoolean();
            float connectionLineOpacity = ois.readFloat();

            // Load planet color (backwards compatibility)
            Color playerPlanetColor = Color.BLUE;
            try {
                playerPlanetColor = (Color) ois.readObject();
            } catch (Exception colorEx) {
                // If color loading fails, use default
            }

            // Load keybinds (backwards compatibility)
            Map<Integer, AbilityType> keybindMap = null;
            try {
                keybindMap = (Map<Integer, AbilityType>) ois.readObject();
            } catch (Exception keybindEx) {
                // If keybind loading fails, use null (will trigger default keybinds)
            }

            System.out.println("Visual settings loaded successfully");
            return new VisualSettingsContainer(displayConnectionLines, displayEffects, displayProjectiles,
                    displayPlanetMoons, displayShips, connectionLineOpacity, playerPlanetColor, keybindMap);

        } catch (IOException e) {
            // Use default settings if file doesn't exist or can't be read
            System.out.println("Could not load visual settings, using defaults");
            return new VisualSettingsContainer(); // Default values
        }
    }
}