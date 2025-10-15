package home.game;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import home.game.abilities.AbilityType;
import home.game.io.SaveLoadManager;
import home.game.io.datacontainers.VisualSettingsContainer;

/**
 * Manages visual display settings for the game
 */
public class VisualSettings {
    private static VisualSettings instance;

    // Visual toggles
    private boolean displayConnectionLines = true;
    private boolean displayEffects = true;
    private boolean displayProjectiles = true;
    private boolean displayPlanetMoons = true;
    private boolean displayShips = true;

    // Visual adjustments
    private float connectionLineOpacity = 0.6f; // 0.0 to 1.0

    // Planet customization
    private Color playerPlanetColor = Color.BLUE; // Default blue color

    // Keybind mappings - maps key codes to ability types
    private Map<Integer, AbilityType> keybindMap = new HashMap<>();

    // Default keybinds (will be applied if no saved keybinds exist)
    private static final int[] DEFAULT_KEYS = {
            KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5,
            KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0,
            KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS
    };

    // Predefined color options for players
    public static final Color[] AVAILABLE_PLANET_COLORS = {
            Color.BLUE, // Classic blue
            Color.CYAN, // Cyan
            new Color(0, 150, 255), // Light blue
            new Color(0, 100, 200), // Dark blue
            new Color(128, 0, 128), // Purple
            new Color(255, 20, 147), // Deep pink
            new Color(0, 255, 127), // Spring green
            new Color(255, 69, 0), // Orange red
            new Color(255, 215, 0), // Gold
            new Color(50, 205, 50), // Lime green
            new Color(220, 20, 60), // Crimson
            new Color(138, 43, 226), // Blue violet
            new Color(255, 105, 180), // Hot pink
            new Color(0, 191, 255), // Deep sky blue
            new Color(255, 140, 0), // Dark orange
            new Color(154, 205, 50), // Yellow green
            new Color(75, 0, 130), // Indigo
            new Color(255, 192, 203), // Light pink
            new Color(165, 42, 42), // Brown
            new Color(0, 128, 128), // Teal
            new Color(255, 215, 180), // Peach
            new Color(70, 130, 180), // Steel blue
            new Color(255, 0, 255) // Magenta
    };

    public static Color[] getAvailablePlanetColors() {
        return AVAILABLE_PLANET_COLORS;
    }

    // Color names corresponding to AVAILABLE_PLANET_COLORS
    public static final String[] PLANET_COLOR_NAMES = {
            "Classic Blue",
            "Cyan",
            "Light Blue",
            "Dark Blue",
            "Purple",
            "Deep Pink",
            "Spring Green",
            "Orange Red",
            "Gold",
            "Lime Green",
            "Crimson",
            "Blue Violet",
            "Hot Pink",
            "Deep Sky Blue",
            "Dark Orange",
            "Yellow Green",
            "Indigo",
            "Light Pink",
            "Brown",
            "Teal",
            "Peach",
            "Steel Blue",
            "Magenta"
    };

    private VisualSettings() {
        loadSettings();
    }

    public static VisualSettings getInstance() {
        if (instance == null) {
            instance = new VisualSettings();
        }
        return instance;
    }

    // Getters
    public boolean isDisplayConnectionLines() {
        return displayConnectionLines;
    }

    public boolean isDisplayEffects() {
        return displayEffects;
    }

    public boolean isDisplayProjectiles() {
        return displayProjectiles;
    }

    public boolean isDisplayPlanetMoons() {
        return displayPlanetMoons;
    }

    public boolean isDisplayShips() {
        return displayShips;
    }

    public float getConnectionLineOpacity() {
        return connectionLineOpacity;
    }

    public Color getPlayerPlanetColor() {
        return playerPlanetColor;
    }

    public Map<Integer, AbilityType> getKeybindMap() {
        return new HashMap<>(keybindMap);
    }

    public AbilityType getAbilityForKey(int keyCode) {
        return keybindMap.get(keyCode);
    }

    public Integer getKeyForAbility(AbilityType ability) {
        for (Map.Entry<Integer, AbilityType> entry : keybindMap.entrySet()) {
            if (entry.getValue() == ability) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getKeyName(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_1:
                return "1";
            case KeyEvent.VK_2:
                return "2";
            case KeyEvent.VK_3:
                return "3";
            case KeyEvent.VK_4:
                return "4";
            case KeyEvent.VK_5:
                return "5";
            case KeyEvent.VK_6:
                return "6";
            case KeyEvent.VK_7:
                return "7";
            case KeyEvent.VK_8:
                return "8";
            case KeyEvent.VK_9:
                return "9";
            case KeyEvent.VK_0:
                return "0";
            case KeyEvent.VK_MINUS:
                return "-";
            case KeyEvent.VK_EQUALS:
                return "=";
            default:
                return "?";
        }
    }

    // Setters
    public void setDisplayConnectionLines(boolean displayConnectionLines) {
        this.displayConnectionLines = displayConnectionLines;
        saveSettings();
    }

    public void setDisplayEffects(boolean displayEffects) {
        this.displayEffects = displayEffects;
        saveSettings();
    }

    public void setDisplayProjectiles(boolean displayProjectiles) {
        this.displayProjectiles = displayProjectiles;
        saveSettings();
    }

    public void setDisplayPlanetMoons(boolean displayPlanetMoons) {
        this.displayPlanetMoons = displayPlanetMoons;
        saveSettings();
    }

    public void setDisplayShips(boolean displayShips) {
        this.displayShips = displayShips;
        saveSettings();
    }

    public void setConnectionLineOpacity(float opacity) {
        this.connectionLineOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
        saveSettings();
    }

    public void setPlayerPlanetColor(Color color) {
        this.playerPlanetColor = color;
        saveSettings();
    }

    /**
     * Set player planet color by index from AVAILABLE_PLANET_COLORS
     */
    public void setPlayerPlanetColorByIndex(int index) {
        if (index >= 0 && index < AVAILABLE_PLANET_COLORS.length) {
            setPlayerPlanetColor(AVAILABLE_PLANET_COLORS[index]);
        }
    }

    public void setKeybind(int keyCode, AbilityType ability) {
        // Remove any existing binding for this ability
        keybindMap.entrySet().removeIf(entry -> entry.getValue() == ability);

        if (ability != null) {
            keybindMap.put(keyCode, ability);
        } else {
            keybindMap.remove(keyCode);
        }
        saveSettings();
    }

    public void removeKeybind(int keyCode) {
        keybindMap.remove(keyCode);
        saveSettings();
    }

    public void clearKeybind(AbilityType ability) {
        keybindMap.entrySet().removeIf(entry -> entry.getValue() == ability);
        saveSettings();
    }

    /**
     * Get the index of the current player planet color
     */
    public int getPlayerPlanetColorIndex() {
        for (int i = 0; i < AVAILABLE_PLANET_COLORS.length; i++) {
            if (AVAILABLE_PLANET_COLORS[i].equals(playerPlanetColor)) {
                return i;
            }
        }
        return 0; // Default to first color if not found
    }

    /**
     * Get a ship color that's a lighter variant of the player's planet color
     */
    public Color getPlayerShipColor() {
        // Create a lighter, more saturated version of the planet color for ships
        Color baseColor = playerPlanetColor;

        // Get RGB components
        int red = baseColor.getRed();
        int green = baseColor.getGreen();
        int blue = baseColor.getBlue();

        // Make it lighter and more vibrant
        red = Math.min(255, (int) (red * 1.3f + 40));
        green = Math.min(255, (int) (green * 1.3f + 40));
        blue = Math.min(255, (int) (blue * 1.3f + 40));

        return new Color(red, green, blue);
    }

    /**
     * Load settings from file
     */
    private void loadSettings() {
        VisualSettingsContainer settings = SaveLoadManager.getInstance().loadVisualSettings();

        this.displayConnectionLines = settings.displayConnectionLines;
        this.displayEffects = settings.displayEffects;
        this.displayProjectiles = settings.displayProjectiles;
        this.displayPlanetMoons = settings.displayPlanetMoons;
        this.displayShips = settings.displayShips;
        this.connectionLineOpacity = settings.connectionLineOpacity;
        this.playerPlanetColor = settings.playerPlanetColor;

        // Load keybinds or set defaults
        if (settings.keybindMap != null) {
            this.keybindMap = new HashMap<>(settings.keybindMap);
        } else {
            // Set default keybinds for the first few abilities
            initializeDefaultKeybinds();
        }
    }

    private void initializeDefaultKeybinds() {
        keybindMap.clear();
        AbilityType[] abilities = AbilityType.values();

        // Map keys 1-9, 0, -, = to the first 12 abilities
        for (int i = 0; i < Math.min(DEFAULT_KEYS.length, abilities.length); i++) {
            keybindMap.put(DEFAULT_KEYS[i], abilities[i]);
        }
    }

    /**
     * Save settings to file
     */
    private void saveSettings() {
        SaveLoadManager.getInstance().saveVisualSettings(displayConnectionLines, displayEffects,
                displayProjectiles, displayPlanetMoons,
                displayShips, connectionLineOpacity,
                playerPlanetColor, keybindMap);
    }
}