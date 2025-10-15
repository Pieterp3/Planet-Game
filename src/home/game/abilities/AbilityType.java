package home.game.abilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import home.game.GameConstants;
import home.game.VisualSettings;

// Ability types enum
public enum AbilityType {
    FREEZE("Freeze", "❄️", "Stops enemy planets from spawning ships"),
    MISSILE_BARRAGE("Missile Barrage", "🚀", "Sends missiles at random enemy planets"),
    SHIELD("Shield", "🛡️", "Makes your planets and ships invulnerable"),
    FACTORY_HYPE("Factory Hype", "⚡", "Your planets spawn ships 3x faster"),
    IMPROVED_FACTORIES("Improved Factories", "🔧", "Doubles ship speed, damage and health"),
    ANSWERED_PRAYERS("Answered Prayers", "💚", "Heals all damaged planets"),
    CURSE("Curse", "💜", "Places curse on enemy planets, reducing their ship stats by 20%"),
    BLACK_HOLE("Black Hole", "⚫", "Spawns black hole that damages enemy planets in orbit"),
    PLANETARY_FLAME("Planetary Flame", "🔥", "Spews fire from player planets, damaging enemies"),
    PLANETARY_INFECTION("Planetary Infection", "🦠", "Infects enemy planets to send ships for you"),
    UNSTOPPABLE_SHIPS("Unstoppable Ships", "⚔️", "Makes your ships invulnerable for a duration"),
    ORBITAL_FREEZE("Orbital Freeze", "🧊", "Freezes planets in their orbital position completely");

    private final String displayName;
    private final String icon;
    private final String description;

    AbilityType(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Draws the ability icon at the specified position and size
     * 
     * @param g2d         Graphics2D context
     * @param x           X position (center)
     * @param y           Y position (center)
     * @param size        Icon size
     * @param showKeybind Whether to display the keybind on the icon
     */
    public void drawIcon(Graphics2D g2d, int x, int y, int size, boolean showKeybind) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int halfSize = size / 2;

        switch (this) {
            case FREEZE -> {
                // Draw snowflake
                g2d.setColor(new Color(150, 200, 255));
                // Main cross
                g2d.drawLine(x - halfSize + 2, y, x + halfSize - 2, y);
                g2d.drawLine(x, y - halfSize + 2, x, y + halfSize - 2);
                // Diagonal lines
                int diag = halfSize - 4;
                g2d.drawLine(x - diag, y - diag, x + diag, y + diag);
                g2d.drawLine(x - diag, y + diag, x + diag, y - diag);
                // Small branches
                int branch = 3;
                g2d.drawLine(x - halfSize + 2 + branch, y - branch, x - halfSize + 2, y);
                g2d.drawLine(x - halfSize + 2 + branch, y + branch, x - halfSize + 2, y);
                g2d.drawLine(x + halfSize - 2 - branch, y - branch, x + halfSize - 2, y);
                g2d.drawLine(x + halfSize - 2 - branch, y + branch, x + halfSize - 2, y);
            }
            case MISSILE_BARRAGE -> {
                // Draw rocket
                g2d.setColor(new Color(255, 100, 100));
                // Rocket body
                g2d.fillOval(x - 3, y - halfSize + 2, 6, size - 8);
                // Rocket tip
                int[] tipX = { x - 3, x + 3, x };
                int[] tipY = { y - halfSize + 2, y - halfSize + 2, y - halfSize - 2 };
                g2d.fillPolygon(tipX, tipY, 3);
                // Flames
                g2d.setColor(new Color(255, 150, 50));
                int[] flameX = { x - 4, x + 4, x - 2, x + 2, x };
                int[] flameY = { y + halfSize - 2, y + halfSize - 2, y + halfSize + 2, y + halfSize + 2,
                        y + halfSize + 4 };
                g2d.fillPolygon(flameX, flameY, 5);
            }
            case SHIELD -> {
                // Draw shield
                g2d.setColor(new Color(100, 150, 255));
                // Shield outline
                int[] shieldX = { x, x - halfSize + 2, x - halfSize + 4, x - halfSize + 6, x, x + halfSize - 6,
                        x + halfSize - 4, x + halfSize - 2 };
                int[] shieldY = { y - halfSize + 2, y - 2, y + 2, y + halfSize - 4, y + halfSize - 2,
                        y + halfSize - 4, y + 2, y - 2 };
                g2d.fillPolygon(shieldX, shieldY, 8);
                // Shield cross
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(x, y - halfSize + 6, x, y + halfSize - 6);
                g2d.drawLine(x - halfSize + 6, y, x + halfSize - 6, y);
            }
            case FACTORY_HYPE -> {
                // Draw lightning bolt
                g2d.setColor(new Color(255, 255, 100));
                int[] boltX = { x - 2, x + 2, x - 1, x + 4, x - 3, x + 1, x - 2 };
                int[] boltY = { y - halfSize + 2, y - 2, y, y + halfSize - 2, y + 2, y, y - halfSize + 2 };
                g2d.fillPolygon(boltX, boltY, 6);
                // Glow effect
                g2d.setColor(new Color(255, 255, 200, 100));
                g2d.fillOval(x - halfSize + 1, y - halfSize + 1, size - 2, size - 2);
            }
            case IMPROVED_FACTORIES -> {
                // Draw wrench
                g2d.setColor(new Color(150, 150, 150));
                // Handle
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.drawLine(x - 2, y - halfSize + 4, x - 2, y + halfSize - 4);
                // Head
                g2d.fillOval(x - 5, y - halfSize + 2, 6, 6);
                // Jaw
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawLine(x + 1, y - halfSize + 3, x + 3, y - halfSize + 5);
            }
            case ANSWERED_PRAYERS -> {
                // Draw heart
                g2d.setColor(new Color(100, 255, 100));
                // Two circles for heart top
                g2d.fillOval(x - halfSize + 2, y - 2, halfSize, halfSize);
                g2d.fillOval(x - 2, y - 2, halfSize, halfSize);
                // Triangle for heart bottom
                int[] heartX = { x - halfSize + 2, x + halfSize - 2, x };
                int[] heartY = { y + 2, y + 2, y + halfSize - 2 };
                g2d.fillPolygon(heartX, heartY, 3);
            }
            case CURSE -> {
                // Draw purple smoke/curse effect
                g2d.setColor(new Color(150, 50, 200));
                // Draw wavy smoke lines
                x += 10;// Shift right for better centering
                for (int i = 0; i < 3; i++) {
                    int startX = x - halfSize + 3 + (i * 6);
                    int startY = y + halfSize - 2;
                    // Create wavy line upward
                    for (int j = 0; j < halfSize + 2; j += 3) {
                        int waveX = startX + (int) (2 * Math.sin(j * 0.3));
                        int currentY = startY - j;
                        int nextY = startY - j - 3;
                        int nextWaveX = startX + (int) (2 * Math.sin((j + 3) * 0.3));
                        g2d.drawLine(waveX, currentY, nextWaveX, nextY);
                    }
                }
                // Add small particles
                g2d.fillOval(x - 2, y - halfSize + 3, 2, 2);
                g2d.fillOval(x + 3, y - halfSize + 6, 2, 2);
                g2d.fillOval(x - 4, y - halfSize + 8, 2, 2);
            }
            case BLACK_HOLE -> {
                // Draw swirling black hole
                g2d.setColor(Color.BLACK);
                g2d.fillOval(x - halfSize, y - halfSize, size, size);
                // Draw accretion disk with gradient effect
                g2d.setColor(new Color(100, 50, 200, 150));
                g2d.drawOval(x - halfSize - 2, y - halfSize - 2, size + 4, size + 4);
                g2d.setColor(new Color(200, 100, 50, 100));
                g2d.drawOval(x - halfSize - 4, y - halfSize - 4, size + 8, size + 8);
                // Draw spiral pattern
                g2d.setColor(new Color(255, 200, 100, 80));
                double angle = 0;
                int prevX = x + halfSize;
                int prevY = y;
                for (int i = 0; i < 20; i++) {
                    angle += 0.5;
                    int radius = (int) (halfSize * (1.0 - i / 20.0));
                    int spiralX = x + (int) (radius * Math.cos(angle));
                    int spiralY = y + (int) (radius * Math.sin(angle));
                    g2d.drawLine(prevX, prevY, spiralX, spiralY);
                    prevX = spiralX;
                    prevY = spiralY;
                }
            }
            case PLANETARY_FLAME -> {
                // Draw planet with flames
                g2d.setColor(new Color(200, 100, 50));
                // Planet body
                g2d.fillOval(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize);
                // Flame towers
                g2d.setColor(new Color(255, 150, 50));
                // Left flame
                int[] flameX1 = { x - halfSize / 2 - 2, x - halfSize / 2, x - halfSize / 2 - 4,
                        x - halfSize / 2 - 2 };
                int[] flameY1 = { y, y - halfSize, y - halfSize - 4, y - halfSize - 6 };
                g2d.fillPolygon(flameX1, flameY1, 4);
                // Right flame
                int[] flameX2 = { x + halfSize / 2 + 2, x + halfSize / 2, x + halfSize / 2 + 4,
                        x + halfSize / 2 + 2 };
                int[] flameY2 = { y, y - halfSize, y - halfSize - 4, y - halfSize - 6 };
                g2d.fillPolygon(flameX2, flameY2, 4);
                // Inner flame core
                g2d.setColor(new Color(255, 255, 100));
                g2d.fillOval(x - halfSize / 2 - 1, y - halfSize + 2, 2, halfSize - 4);
                g2d.fillOval(x + halfSize / 2 - 1, y - halfSize + 2, 2, halfSize - 4);
            }
            case PLANETARY_INFECTION -> {
                // Draw scary virus/infection icon
                g2d.setColor(new Color(50, 150, 50));
                // Main virus body
                g2d.fillOval(x - halfSize / 3, y - halfSize / 3, (halfSize * 2) / 3, (halfSize * 2) / 3);

                // Virus spikes
                g2d.setColor(new Color(100, 200, 100));
                for (int i = 0; i < 8; i++) {
                    double angle = (i * 2 * Math.PI) / 8;
                    int spikeLength = 4;
                    int startX = x + (int) ((halfSize / 3) * Math.cos(angle));
                    int startY = y + (int) ((halfSize / 3) * Math.sin(angle));
                    int endX = x + (int) ((halfSize / 3 + spikeLength) * Math.cos(angle));
                    int endY = y + (int) ((halfSize / 3 + spikeLength) * Math.sin(angle));

                    g2d.drawLine(startX, startY, endX, endY);
                    // Draw small spike tip
                    g2d.fillOval(endX - 1, endY - 1, 2, 2);
                }

                // Virus core pattern
                g2d.setColor(new Color(0, 100, 0));
                g2d.fillOval(x - 2, y - 2, 4, 4);
                g2d.fillOval(x - 4, y - 1, 2, 2);
                g2d.fillOval(x + 2, y - 1, 2, 2);
            }
            case UNSTOPPABLE_SHIPS -> {
                // Draw sword/weapon icon representing unstoppable force
                g2d.setColor(new Color(200, 200, 255));

                // Sword blade
                int[] swordX = { x, x - 2, x - 2, x + 2, x + 2 };
                int[] swordY = { y - halfSize + 2, y - halfSize + 6, y + 2, y + 2, y - halfSize + 6 };
                g2d.fillPolygon(swordX, swordY, 5);

                // Sword hilt
                g2d.setColor(new Color(150, 150, 100));
                g2d.fillRect(x - 4, y + 2, 8, 3);

                // Sword grip
                g2d.setColor(new Color(100, 100, 50));
                g2d.fillRect(x - 2, y + 5, 4, 6);

                // Energy aura around sword
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawOval(x - halfSize + 1, y - halfSize + 1, size - 2, size - 2);

                // Sparks/energy particles
                g2d.setColor(new Color(255, 255, 200, 150));
                for (int i = 0; i < 6; i++) {
                    double angle = (i * Math.PI) / 3;
                    int sparkX = x + (int) ((halfSize - 2) * Math.cos(angle));
                    int sparkY = y + (int) ((halfSize - 2) * Math.sin(angle));
                    g2d.fillOval(sparkX - 1, sparkY - 1, 2, 2);
                }
            }
            case ORBITAL_FREEZE -> {
                // Draw detailed solid ice ball
                g2d.setColor(new Color(180, 220, 255)); // Light ice blue
                // Main ice sphere
                g2d.fillOval(x - halfSize, y - halfSize, size, size);

                // Ice crystal facets - darker blue lines for definition
                g2d.setColor(new Color(120, 180, 220));
                g2d.setStroke(new BasicStroke(1.5f));

                // Vertical crystal lines
                g2d.drawLine(x - halfSize / 3, y - halfSize + 2, x - halfSize / 3, y + halfSize - 2);
                g2d.drawLine(x + halfSize / 3, y - halfSize + 2, x + halfSize / 3, y + halfSize - 2);

                // Horizontal crystal lines
                g2d.drawLine(x - halfSize + 2, y - halfSize / 3, x + halfSize - 2, y - halfSize / 3);
                g2d.drawLine(x - halfSize + 2, y + halfSize / 3, x + halfSize - 2, y + halfSize / 3);

                // Diagonal crystal lines
                g2d.drawLine(x - halfSize / 2, y - halfSize / 2, x + halfSize / 2, y + halfSize / 2);
                g2d.drawLine(x - halfSize / 2, y + halfSize / 2, x + halfSize / 2, y - halfSize / 2);

                // Ice highlights - bright white spots
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillOval(x - halfSize / 2, y - halfSize / 2, 3, 3);
                g2d.fillOval(x + halfSize / 3, y - halfSize / 3, 2, 2);
                g2d.fillOval(x - halfSize / 4, y + halfSize / 4, 2, 2);

                // Outer ice rim with transparency
                g2d.setColor(new Color(200, 240, 255, 100));
                g2d.drawOval(x - halfSize - 1, y - halfSize - 1, size + 2, size + 2);
            }
        }

        // Draw keybind if requested
        if (showKeybind) {
            VisualSettings settings = VisualSettings.getInstance();
            Integer keyCode = settings.getKeyForAbility(this);

            if (keyCode != null) {
                String keyName = VisualSettings.getKeyName(keyCode);

                // Set up font and get metrics
                Font font = new Font("Arial", Font.BOLD, Math.max(8, size / 4));
                g2d.setFont(font);
                FontMetrics metrics = g2d.getFontMetrics();

                // Calculate text position (bottom right corner of icon)
                int textX = x + halfSize - metrics.stringWidth(keyName) - 2;
                int textY = y + halfSize - 2;

                // Draw background for better readability
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(textX - 2, textY - metrics.getHeight() + 2,
                        metrics.stringWidth(keyName) + 4, metrics.getHeight());

                // Draw the keybind text
                g2d.setColor(Color.WHITE);
                g2d.drawString(keyName, textX, textY);
            }
        }
    }

    /**
     * Draws the ability icon at the specified position and size (without keybind)
     * 
     * @param g2d  Graphics2D context
     * @param x    X position (center)
     * @param y    Y position (center)
     * @param size Icon size
     */
    public void drawIcon(Graphics2D g2d, int x, int y, int size) {
        drawIcon(g2d, x, y, size, false);
    }

    public int getBaseCost() {
        return switch (this) {
            case FREEZE -> 150; // Base cost equivalent to ~5 minutes gameplay
            case SHIELD -> 150; // Same as freeze
            case ANSWERED_PRAYERS -> 100; // Less expensive than freeze/shield
            case MISSILE_BARRAGE -> 250; // More expensive than freeze
            case FACTORY_HYPE -> 350; // More expensive than missile barrage
            case IMPROVED_FACTORIES -> 250; // Same as missile barrage
            case CURSE -> 400; // Expensive, in line with more expensive abilities
            case BLACK_HOLE -> 400; // Same as curse
            case PLANETARY_FLAME -> 500; // Most expensive ability
            case PLANETARY_INFECTION -> 300; // Between curse and factory hype
            case UNSTOPPABLE_SHIPS -> 150; // In line with shield and freeze
            case ORBITAL_FREEZE -> 250; // Same base cost as missile barrage
        };
    }

    public int getUpgradeCost(int currentLevel) {
        int baseCost = getBaseCost();
        return switch (this) {
            case FREEZE -> (int) (baseCost * 0.3 * Math.pow(1.15, currentLevel));
            case SHIELD -> (int) (baseCost * 0.3 * Math.pow(1.15, currentLevel));
            case ANSWERED_PRAYERS -> (int) (baseCost * 0.5 * Math.pow(1.2, currentLevel)); // Upgrades cost more
            case MISSILE_BARRAGE -> (int) (baseCost * 0.4 * Math.pow(1.18, currentLevel));
            case FACTORY_HYPE -> (int) (baseCost * 0.4 * Math.pow(1.18, currentLevel));
            case IMPROVED_FACTORIES -> (int) (baseCost * 0.4 * Math.pow(1.18, currentLevel));
            case CURSE -> (int) (baseCost * 0.6 * Math.pow(1.35, currentLevel)); // Extremely expensive upgrades
            case BLACK_HOLE -> (int) (baseCost * 0.5 * Math.pow(1.25, currentLevel)); // Expensive but not as fast
                                                                                      // as curse
            case PLANETARY_FLAME -> (int) (baseCost * 0.7 * Math.pow(1.30, currentLevel)); // Expensive and rapid
                                                                                           // growth
            case PLANETARY_INFECTION -> (int) (baseCost * 0.7 * Math.pow(1.40, currentLevel)); // More expensive
                                                                                               // than curse
                                                                                               // upgrades
            case UNSTOPPABLE_SHIPS -> (int) (baseCost * 0.3 * Math.pow(1.15, currentLevel)); // Same as
                                                                                             // freeze/shield
            case ORBITAL_FREEZE -> (int) (baseCost * 0.5 * Math.pow(1.25, currentLevel)); // Same rate as black hole
        };
    }

    public double calculateDuration(int level) {
        return switch (this) {
            case FREEZE -> 3.0 + (level * 0.5); // 3s + 0.5s per level
            case SHIELD -> 3.0 + (level * 0.5); // 3s + 0.5s per level
            case FACTORY_HYPE -> 3.0 + (level * 0.5); // 3s + 0.5s per level
            case IMPROVED_FACTORIES -> 3.0 + (level * 0.5); // 3s + 0.5s per level
            case CURSE -> 20.0 + (level * 2.0); // Long duration, semi-permanent
            case BLACK_HOLE -> 8.0 + (level * 1.0); // 8s + 1s per level
            case PLANETARY_FLAME -> 4.0 + (level * 0.5); // 4s + 0.5s per level
            case PLANETARY_INFECTION -> 10.0; // Fixed 10 second duration (dies out after 10 seconds)
            case UNSTOPPABLE_SHIPS -> 3.0 + (level * 0.5); // 3s + 0.5s per level
            case ORBITAL_FREEZE -> 10.0; // Fixed 10 second duration, does not increase with upgrades
            case MISSILE_BARRAGE, ANSWERED_PRAYERS -> 0; // Instant abilities
        };
    }

    public int calculatePower(int level) {
        return switch (this) {
            case MISSILE_BARRAGE -> 5 + level; // 5 + 1 per level
            case ANSWERED_PRAYERS -> (int) (25 + (level * 1.5)); // 25% + 1.5% per level, capped at 100%
            case CURSE -> (int) (20 + (level * 1.6)); // 20% + 1.6% per level stat reduction
            case BLACK_HOLE ->
                GameConstants.getBlackHoleBasePower() + (level * GameConstants.getBlackHolePowerPerLevel());
            case PLANETARY_FLAME -> GameConstants.getFlameBasePower() + (level * GameConstants.getFlamePowerPerLevel());
            case PLANETARY_INFECTION -> Math.min(GameConstants.getMaxPlanets(), 1 + level); // 1 + 1 per level, capped
            // at max planets
            case ORBITAL_FREEZE -> Math.min(GameConstants.getMaxPlanets(), 1 + level); // 1 + 1 per level, capped at max
                                                                                       // planets
            case FREEZE, SHIELD, FACTORY_HYPE, IMPROVED_FACTORIES, UNSTOPPABLE_SHIPS -> 0; // No power value for
                                                                                           // these
        };
    }

    public String getTooltipText(int level) {
        return switch (this) {
            case FREEZE ->
                String.format("%s\n%s\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case MISSILE_BARRAGE ->
                String.format("%s\n%s\nMissiles: %d\nCooldown: %ds", displayName, description,
                        calculatePower(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case SHIELD ->
                String.format("%s\n%s\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case FACTORY_HYPE ->
                String.format("%s\n%s\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case IMPROVED_FACTORIES ->
                String.format("%s\n%s\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case ANSWERED_PRAYERS -> String.format("%s\n%s\nHealing: %d%%\nCooldown: %ds", displayName, description,
                    Math.min(100, calculatePower(level)), GameConstants.getBaseAbilityCooldown() / 1000);
            case CURSE -> String.format("%s\n%s\nStat Reduction: %d%%\nDuration: %.1fs\nCooldown: %ds", displayName,
                    description,
                    calculatePower(level), calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case BLACK_HOLE ->
                String.format("%s\n%s\nEvent Horizon: %d\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        Math.min(150, calculatePower(level)), calculateDuration(level),
                        GameConstants.getBaseAbilityCooldown() / 1000);
            case PLANETARY_FLAME ->
                String.format("%s\n%s\nFlame Power: %d\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        calculatePower(level), calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case PLANETARY_INFECTION ->
                String.format("%s\n%s\nInfected Planets: %d\nDuration: %.1fs\nCooldown: %ds", displayName,
                        description,
                        calculatePower(level), calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case UNSTOPPABLE_SHIPS ->
                String.format("%s\n%s\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
            case ORBITAL_FREEZE ->
                String.format("%s\n%s\nFrozen Planets: %d\nDuration: %.1fs\nCooldown: %ds", displayName, description,
                        calculatePower(level), calculateDuration(level), GameConstants.getBaseAbilityCooldown() / 1000);
        };
    }
}