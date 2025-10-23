package home.game.io.datacontainers;

import java.awt.Color;
import java.util.Map;

import home.game.abilities.AbilityType;

public class VisualSettingsContainer {
    public final boolean displayConnectionLines;
    public final boolean displayEffects;
    public final boolean displayProjectiles;
    public final boolean displayPlanetMoons;
    public final boolean displayShips;
    public final float connectionLineOpacity;
    public final Color playerPlanetColor;
    public final Map<Integer, AbilityType> keybindMap;

    // Sound settings
    public final boolean soundEnabled;
    public final double masterVolume;
    public final boolean reverbEnabled;

    public VisualSettingsContainer() {
        // Default values
        this.displayConnectionLines = true;
        this.displayEffects = true;
        this.displayProjectiles = true;
        this.displayPlanetMoons = true;
        this.displayShips = true;
        this.connectionLineOpacity = 0.6f;
        this.playerPlanetColor = Color.BLUE;
        this.keybindMap = null;

        // Default sound settings
        this.soundEnabled = true;
        this.masterVolume = 0.7;
        this.reverbEnabled = false;
    }

    public VisualSettingsContainer(boolean displayConnectionLines, boolean displayEffects, boolean displayProjectiles,
            boolean displayPlanetMoons, boolean displayShips, float connectionLineOpacity, Color playerPlanetColor,
            Map<Integer, AbilityType> keybindMap, boolean soundEnabled, double masterVolume, boolean reverbEnabled) {
        this.displayConnectionLines = displayConnectionLines;
        this.displayEffects = displayEffects;
        this.displayProjectiles = displayProjectiles;
        this.displayPlanetMoons = displayPlanetMoons;
        this.displayShips = displayShips;
        this.connectionLineOpacity = connectionLineOpacity;
        this.playerPlanetColor = playerPlanetColor;
        this.keybindMap = keybindMap;

        // Sound settings
        this.soundEnabled = soundEnabled;
        this.masterVolume = masterVolume;
        this.reverbEnabled = reverbEnabled;
    }
}
