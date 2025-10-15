package home.game;

import home.game.io.SaveLoadManager;
import java.util.*;

public class GameConstants {

    private static String TITLE_DEFAULT_RESTORATION = "Set to true to restore default configuration.";
    private static boolean RESTORE_DEFAULTS = false;

    // Engine & Performance Constants
    private static String TITLE_ENGINE_AND_PERFORMANCE = "Engine & Performance";
    private static int TARGET_TPS = 60; // Ticks per second
    private static int SLOW_MODE_TPS = 20; // Slow mode for targeting
    private static long PAUSE_SLEEP_INTERVAL = 100; // Milliseconds to sleep while paused
    private static long ENGINE_CPU_RELIEF_SLEEP = 1; // Prevent CPU overload

    // UI Refresh & Notification Timing
    private static String TITLE_UI = "UI And Frame Info and Timing";
    private static long NOTIFICATION_DISPLAY_DURATION = 5000; // Achievement notifications (5s)
    private static long PROGRESS_NOTIFICATION_DURATION = 3000; // Progress notifications (3s)
    private static long CHALLENGE_SAVE_DELAY = 2000; // Delayed save buffer (2s)
    private static int GAME_WIDTH = 1200;
    private static int GAME_HEIGHT = 850;

    // Ability Base Configuration
    private static String TITLE_ABILITY_SYSTEM = "Ability System";
    private static long BASE_ABILITY_COOLDOWN = 45000; // Base cooldown (45 seconds)
    private static double MISSILE_DAMAGE_MULTIPLIER = 2.0; // Missile damage vs normal projectiles
    private static double MISSILE_SPEED_MULTIPLIER = 2.0; // Missile speed vs normal projectiles
    private static double ABILITY_COOLDOWN_REDUCTION_PERCENT = 100.0; // For upgrade calculations

    // Black Hole Constants
    private static int BLACK_HOLE_BASE_POWER = 300; // Base event horizon diameter
    private static int BLACK_HOLE_POWER_PER_LEVEL = 10; // Power increase per upgrade level

    // Planetary Flame Constants

    private static int FLAME_BASE_POWER = 500; // Base flame length and damage
    private static int FLAME_POWER_PER_LEVEL = 20; // Power increase per upgrade level

    // Orbital Mechanics
    private static String TITLE_ORBITAL_MECHANICS = "Orbital Mechanics";
    private static double MIN_ORBIT_RADIUS = 80.0; // Minimum distance from center star
    private static double MAX_ORBIT_RADIUS_FACTOR = 0.9; // Factor of screen dimensions for max orbit
    private static double MIN_ORBITAL_SPEED = 0.005; // Minimum radians per tick
    private static double MAX_ORBITAL_SPEED = 0.02; // Maximum radians per tick
    private static double ORBIT_RADIUS_MARGIN = 50.0; // Margin from screen edge

    // Currency & Rewards
    private static String TITLE_ECONOMY_AND_REWARDS = "Economy & Rewards";
    private static int BASE_COIN_REWARD = 10; // Base coins for 30 seconds of play

    // Difficulty Reward Multipliers
    private static double EASY_REWARD_MULTIPLIER = 1.0;
    private static double MEDIUM_REWARD_MULTIPLIER = 1.2;
    private static double HARD_REWARD_MULTIPLIER = 1.5;
    private static double EXTREME_REWARD_MULTIPLIER = 2.0;

    // Combat System Extended
    private static String TITLE_COMBAT_SYSTEM = "Combat System";
    private static double COMBAT_DISENGAGEMENT_DISTANCE = 120.0; // Hysteresis for combat exit
    private static int MAX_PLANET_HEALTH = 10000;
    private static double DEFAULT_SHIPS_PER_SECOND = 0.5; // 1 ship every 2 seconds
    private static int PLANET_HEALTH_REGEN_RATE = 85; // Health per second
    private static double DEFAULT_SHIP_SPEED = 3.5; // Units per tick
    private static int DEFAULT_SHIP_HEALTH = 1000;
    private static int DEFAULT_SHIP_DAMAGE = 500; // Damage per hit
    private static int PLANET_SIZE = 35;
    private static int SHIP_SIZE = 8; // Ship collision radius
    private static int MAX_PLANETS = 20; // Maximum number of planets in a game
    private static int MIN_PLANETS = 5; // Minimum number of planets in a game
    private static double COMBAT_ENGAGEMENT_DISTANCE = 80; // Distance at which ships start combat
    private static double PROJECTILE_SPEED = 6.5; // Speed of projectiles
    private static double PROJECTILE_MAX_RANGE = 225; // Maximum range of projectiles
    private static long SHIP_FIRE_RATE = 500; // Milliseconds between shots
    private static int PROJECTILE_SIZE = 3; // Projectile visual size

    // Debugging constants
    // Set to true to make player planets invincible for testing
    private static String TITLE_DEBUG_AND_TESTING = "Debug & Testing";
    private static boolean PLAYER_PLANETS_INVINCIBLE = false;
    private static boolean PLAYER_SHIPS_INVINCIBLE = false; // Set to true to make player ships invincible for testing
    private static double DEBUG_COINS_MULTIPLIER = 1.0; // Set to >1 to increase coins earned for testing
    private static boolean PRINT_DEBUG_TO_FILE = false; // Set to true to log debug info to file
    private static boolean REMOVE_ABILITY_COOLDOWNS = false; // Set to true to remove ability cooldowns for testing

    public static int getMaxPlanetHealth() {
        return MAX_PLANET_HEALTH;
    }

    public static double getDefaultShipsPerSecond() {
        return DEFAULT_SHIPS_PER_SECOND;
    }

    public static int getGameWidth() {
        return GAME_WIDTH;
    }

    public static int getGameHeight() {
        return GAME_HEIGHT;
    }

    public static int getPlanetHealthRegenRate() {
        return PLANET_HEALTH_REGEN_RATE;
    }

    public static double getDefaultShipSpeed() {
        return DEFAULT_SHIP_SPEED;
    }

    public static int getDefaultShipHealth() {
        return DEFAULT_SHIP_HEALTH;
    }

    public static int getDefaultShipDamage() {
        return DEFAULT_SHIP_DAMAGE;
    }

    public static int getPlanetSize() {
        return PLANET_SIZE;
    }

    public static int getShipSize() {
        return SHIP_SIZE;
    }

    public static int getMaxPlanets() {
        return MAX_PLANETS;
    }

    public static int getMinPlanets() {
        return MIN_PLANETS;
    }

    public static double getCombatEngagementDistance() {
        return COMBAT_ENGAGEMENT_DISTANCE;
    }

    public static double getProjectileSpeed() {
        return PROJECTILE_SPEED;
    }

    public static double getProjectileMaxRange() {
        return PROJECTILE_MAX_RANGE;
    }

    public static long getShipFireRate() {
        return SHIP_FIRE_RATE;
    }

    public static int getProjectileSize() {
        return PROJECTILE_SIZE;
    }

    public static boolean arePlayerPlanetsInvincible() {
        return PLAYER_PLANETS_INVINCIBLE;
    }

    public static boolean arePlayerShipsInvincible() {
        return PLAYER_SHIPS_INVINCIBLE;
    }

    public static double getDebugCoinsMultiplier() {
        return DEBUG_COINS_MULTIPLIER;
    }

    public static boolean printDebugToFile() {
        return PRINT_DEBUG_TO_FILE;
    }

    public static boolean removeAbilityCooldowns() {
        return REMOVE_ABILITY_COOLDOWNS;
    }

    /**
     * Saves current configuration to the properties file using SaveLoadManager.
     */
    // Configuration Categories
    public enum ConfigCategory {
        GAMEPLAY("Gameplay Balance"),
        GRAPHICS("Visual Settings"),
        DEBUG("Debug & Testing"),
        TIMING("Performance & Timing"),
        PROGRESSION("Economy & Upgrades"),
        ABILITIES("Ability System"),
        CHALLENGES("Challenge System");

        private final String displayName;

        ConfigCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Configuration change listeners
    public interface ConfigChangeListener {
        void onConfigurationChanged(String property, Object oldValue, Object newValue);
    }

    private static final List<ConfigChangeListener> listeners = new ArrayList<>();

    // Engine & Performance Getters
    public static int getTargetTPS() {
        return TARGET_TPS;
    }

    public static int getSlowModeTPS() {
        return SLOW_MODE_TPS;
    }

    public static long getPauseSleepInterval() {
        return PAUSE_SLEEP_INTERVAL;
    }

    public static long getEngineCPUReliefSleep() {
        return ENGINE_CPU_RELIEF_SLEEP;
    }

    // UI Timing Getters
    public static long getNotificationDisplayDuration() {
        return NOTIFICATION_DISPLAY_DURATION;
    }

    public static long getProgressNotificationDuration() {
        return PROGRESS_NOTIFICATION_DURATION;
    }

    public static long getChallengeSaveDelay() {
        return CHALLENGE_SAVE_DELAY;
    }

    // Ability System Getters
    public static long getBaseAbilityCooldown() {
        return BASE_ABILITY_COOLDOWN;
    }

    public static double getMissileDamageMultiplier() {
        return MISSILE_DAMAGE_MULTIPLIER;
    }

    public static double getMissileSpeedMultiplier() {
        return MISSILE_SPEED_MULTIPLIER;
    }

    public static double getAbilityCooldownReductionPercent() {
        return ABILITY_COOLDOWN_REDUCTION_PERCENT;
    }

    // Black Hole Getters
    public static int getBlackHoleBasePower() {
        return BLACK_HOLE_BASE_POWER;
    }

    public static int getBlackHolePowerPerLevel() {
        return BLACK_HOLE_POWER_PER_LEVEL;
    }

    // Planetary Flame Getters
    public static int getFlameBasePower() {
        return FLAME_BASE_POWER;
    }

    public static int getFlamePowerPerLevel() {
        return FLAME_POWER_PER_LEVEL;
    }

    // Orbital Mechanics Getters
    public static double getMinOrbitRadius() {
        return MIN_ORBIT_RADIUS;
    }

    public static double getMaxOrbitRadiusFactor() {
        return MAX_ORBIT_RADIUS_FACTOR;
    }

    public static double getMinOrbitalSpeed() {
        return MIN_ORBITAL_SPEED;
    }

    public static double getMaxOrbitalSpeed() {
        return MAX_ORBITAL_SPEED;
    }

    public static double getOrbitRadiusMargin() {
        return ORBIT_RADIUS_MARGIN;
    }

    // Currency & Rewards Getters
    public static int getBaseCoinReward() {
        return BASE_COIN_REWARD;
    }

    // Difficulty Reward Multiplier Getters
    public static double getEasyRewardMultiplier() {
        return EASY_REWARD_MULTIPLIER;
    }

    public static double getMediumRewardMultiplier() {
        return MEDIUM_REWARD_MULTIPLIER;
    }

    public static double getHardRewardMultiplier() {
        return HARD_REWARD_MULTIPLIER;
    }

    public static double getExtremeRewardMultiplier() {
        return EXTREME_REWARD_MULTIPLIER;
    }

    // Combat System Extended Getters
    public static double getCombatDisengagementDistance() {
        return COMBAT_DISENGAGEMENT_DISTANCE;
    }
    // Configuration Management Methods

    /**
     * Add a configuration change listener
     */
    public static void addConfigChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a configuration change listener
     */
    public static void removeConfigChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of a configuration change
     */
    private static void notifyConfigurationChanged(String property, Object oldValue, Object newValue) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigurationChanged(property, oldValue, newValue);
            } catch (Exception e) {
                System.err.println("Error notifying config change listener: " + e.getMessage());
            }
        }
    }

    public static boolean shouldRestoreDefaults() {
        return RESTORE_DEFAULTS;
    }

    /**
     * Hot-reload configuration changes from file
     */
    public static void reloadConfiguration() {
        SaveLoadManager.getInstance().loadGameConstants();
        notifyConfigurationChanged("ALL", null, null);
    }

    /**
     * Validate configuration values to ensure they're within acceptable ranges
     */
    public static void validateConfiguration() {
        // Game dimensions validation
        if (GAME_WIDTH < 800) {
            System.out.println("Warning: GAME_WIDTH too small, setting to 800");
            GAME_WIDTH = 800;
        }
        if (GAME_HEIGHT < 600) {
            System.out.println("Warning: GAME_HEIGHT too small, setting to 600");
            GAME_HEIGHT = 600;
        }

        // Performance validation
        if (TARGET_TPS < 10) {
            System.out.println("Warning: TARGET_TPS too low, setting to 10");
            TARGET_TPS = 10;
        }
        if (TARGET_TPS > 120) {
            System.out.println("Warning: TARGET_TPS too high, setting to 120");
            TARGET_TPS = 120;
        }

        // Combat validation
        if (COMBAT_ENGAGEMENT_DISTANCE <= 0) {
            System.out.println("Warning: COMBAT_ENGAGEMENT_DISTANCE invalid, setting to 80");
            COMBAT_ENGAGEMENT_DISTANCE = 80;
        }
        if (COMBAT_DISENGAGEMENT_DISTANCE <= COMBAT_ENGAGEMENT_DISTANCE) {
            System.out.println("Warning: COMBAT_DISENGAGEMENT_DISTANCE must be > engagement distance");
            COMBAT_DISENGAGEMENT_DISTANCE = COMBAT_ENGAGEMENT_DISTANCE + 40;
        }

        // Health validation
        if (MAX_PLANET_HEALTH <= 0) {
            System.out.println("Warning: MAX_PLANET_HEALTH invalid, setting to 10000");
            MAX_PLANET_HEALTH = 10000;
        }

        // Speed validation
        if (DEFAULT_SHIP_SPEED <= 0) {
            System.out.println("Warning: DEFAULT_SHIP_SPEED invalid, setting to 3.5");
            DEFAULT_SHIP_SPEED = 3.5;
        }

        // Ability validation
        if (BASE_ABILITY_COOLDOWN < 1000) {
            System.out.println("Warning: BASE_ABILITY_COOLDOWN too low, setting to 1000ms");
            BASE_ABILITY_COOLDOWN = 1000;
        }

        // Orbital validation
        if (MIN_ORBIT_RADIUS <= 0
                || MIN_ORBIT_RADIUS >= MAX_ORBIT_RADIUS_FACTOR * Math.min(GAME_WIDTH, GAME_HEIGHT) / 2) {
            System.out.println("Warning: Invalid orbit radius, resetting to defaults");
            MIN_ORBIT_RADIUS = 80.0;
            MAX_ORBIT_RADIUS_FACTOR = 0.9;
        }
    }

    /**
     * Get configuration property by name (for advanced usage)
     */
    public static Object getConfigProperty(String propertyName) {
        try {
            java.lang.reflect.Field field = GameConstants.class.getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            System.err.println("Error getting config property " + propertyName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Set configuration property by name (for advanced usage)
     */
    public static boolean setConfigProperty(String propertyName, Object value) {
        try {
            java.lang.reflect.Field field = GameConstants.class.getDeclaredField(propertyName);
            field.setAccessible(true);
            Object oldValue = field.get(null);
            field.set(null, value);
            notifyConfigurationChanged(propertyName, oldValue, value);
            return true;
        } catch (Exception e) {
            System.err.println("Error setting config property " + propertyName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all configuration properties as a map
     */
    public static Map<String, Object> getAllConfigProperties() {
        Map<String, Object> properties = new HashMap<>();
        try {
            java.lang.reflect.Field[] fields = GameConstants.class.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                        !java.lang.reflect.Modifier.isFinal(field.getModifiers()) &&
                        !field.getName().equals("listeners")) {
                    field.setAccessible(true);
                    properties.put(field.getName(), field.get(null));
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting all config properties: " + e.getMessage());
        }
        return properties;
    }

    public static String[] getTitles() {
        return new String[] {
                TITLE_DEFAULT_RESTORATION,
                TITLE_ENGINE_AND_PERFORMANCE,
                TITLE_UI,
                TITLE_ABILITY_SYSTEM,
                TITLE_ORBITAL_MECHANICS,
                TITLE_ECONOMY_AND_REWARDS,
                TITLE_COMBAT_SYSTEM,
                TITLE_DEBUG_AND_TESTING
        };
    }

    /**
     * Reset all configuration to default values
     */
    public static void resetToDefaults() {
        // Engine & Performance
        TARGET_TPS = 60;
        SLOW_MODE_TPS = 20;
        PAUSE_SLEEP_INTERVAL = 100;
        ENGINE_CPU_RELIEF_SLEEP = 1;

        // UI Timing
        NOTIFICATION_DISPLAY_DURATION = 5000;
        PROGRESS_NOTIFICATION_DURATION = 3000;
        CHALLENGE_SAVE_DELAY = 2000;

        // Abilities
        BASE_ABILITY_COOLDOWN = 45000;
        MISSILE_DAMAGE_MULTIPLIER = 2.0;
        MISSILE_SPEED_MULTIPLIER = 2.0;
        ABILITY_COOLDOWN_REDUCTION_PERCENT = 100.0;

        // Black Hole
        BLACK_HOLE_BASE_POWER = 300;
        BLACK_HOLE_POWER_PER_LEVEL = 10;

        // Flame
        FLAME_BASE_POWER = 500;
        FLAME_POWER_PER_LEVEL = 20;

        // Orbital
        MIN_ORBIT_RADIUS = 80.0;
        MAX_ORBIT_RADIUS_FACTOR = 0.9;
        MIN_ORBITAL_SPEED = 0.005;
        MAX_ORBITAL_SPEED = 0.02;
        ORBIT_RADIUS_MARGIN = 50.0;

        // Economy
        BASE_COIN_REWARD = 10;

        // Difficulty Rewards
        EASY_REWARD_MULTIPLIER = 1.0;
        MEDIUM_REWARD_MULTIPLIER = 1.2;
        HARD_REWARD_MULTIPLIER = 1.5;
        EXTREME_REWARD_MULTIPLIER = 2.0;

        // Combat Extended
        COMBAT_DISENGAGEMENT_DISTANCE = 120.0;

        // Original constants
        MAX_PLANET_HEALTH = 10000;
        DEFAULT_SHIPS_PER_SECOND = 0.5;
        GAME_WIDTH = 1200;
        GAME_HEIGHT = 850;
        PLANET_HEALTH_REGEN_RATE = 85;
        DEFAULT_SHIP_SPEED = 3.5;
        DEFAULT_SHIP_HEALTH = 1000;
        DEFAULT_SHIP_DAMAGE = 500;
        PLANET_SIZE = 35;
        SHIP_SIZE = 8;
        MAX_PLANETS = 20;
        MIN_PLANETS = 5;
        COMBAT_ENGAGEMENT_DISTANCE = 80;
        PROJECTILE_SPEED = 6.5;
        PROJECTILE_MAX_RANGE = 225;
        SHIP_FIRE_RATE = 500;
        PROJECTILE_SIZE = 3;

        // Debug flags
        PLAYER_PLANETS_INVINCIBLE = false;
        PLAYER_SHIPS_INVINCIBLE = false;
        DEBUG_COINS_MULTIPLIER = 1.0;
        PRINT_DEBUG_TO_FILE = false;
        REMOVE_ABILITY_COOLDOWNS = false;

        notifyConfigurationChanged("RESET", null, null);
    }

    /**
     * Saves current configuration to the properties file using SaveLoadManager.
     */
    public static void saveConfiguration() {
        validateConfiguration();
        SaveLoadManager.getInstance().saveGameConstants();
    }

}
