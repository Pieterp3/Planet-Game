package home.frame.gamemenu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import home.game.Game;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.planets.Planet;

public class OperatorIndicatorRenderer {

    private Game game;
    private int width;
    private PlanetArtist planetArtist;
    private EffectsArtist effectsArtist;

    public OperatorIndicatorRenderer(Game game, int width, PlanetArtist planetArtist, EffectsArtist effectsArtist) {
        this.game = game;
        this.width = width;
        this.planetArtist = planetArtist;
        this.effectsArtist = effectsArtist;
    }

    public void renderOperatorIndicators(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Count planets for each operator first
        Map<Operator, Integer> planetCounts = new HashMap<>();
        Map<Operator, List<Planet>> operatorPlanets = new HashMap<>();

        // Initialize for player and all bots
        planetCounts.put(game.getPlayer(), 0);
        operatorPlanets.put(game.getPlayer(), new ArrayList<>());
        for (Operator bot : game.getBots()) {
            planetCounts.put(bot, 0);
            operatorPlanets.put(bot, new ArrayList<>());
        }

        // Count planets owned by each operator and collect them
        for (Planet planet : game.getPlanets()) {
            Operator owner = planet.getOperator();
            if (owner != null && planetCounts.containsKey(owner)) {
                planetCounts.put(owner, planetCounts.get(owner) + 1);
                operatorPlanets.get(owner).add(planet);
            }
        }

        // Create filtered list of operators (only include player and bots with planets)
        List<Operator> activeOperators = new ArrayList<>();
        activeOperators.add(game.getPlayer()); // Always include player

        for (Operator bot : game.getBots()) {
            if (planetCounts.get(bot) > 0) { // Only include bots with planets
                activeOperators.add(bot);
            }
        }

        // Enhanced indicator dimensions
        int indicatorWidth = 140;
        int indicatorHeight = 50; // Increased height for horizontal effects row
        int indicatorSpacing = 15;
        int totalWidth = (activeOperators.size() * indicatorWidth) + ((activeOperators.size() - 1) * indicatorSpacing);
        int startX = (width - totalWidth) / 2; // Center horizontally
        int yPosition = 15; // Top of screen with some margin

        // Render indicators for each active operator
        for (int i = 0; i < activeOperators.size(); i++) {
            Operator operator = activeOperators.get(i);
            int x = startX + (i * (indicatorWidth + indicatorSpacing));

            // Get operator color
            Color operatorColor = planetArtist.getOperatorColor(operator);

            renderEnhancedOperatorIndicator(g, operator, operatorColor, x, yPosition,
                    indicatorWidth, indicatorHeight,
                    planetCounts.get(operator),
                    operatorPlanets.get(operator), i);
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void renderEnhancedOperatorIndicator(Graphics2D g, Operator operator, Color operatorColor,
            int x, int y, int width, int height,
            int planetCount, List<Planet> planets, int operatorIndex) {

        // Create gradient background
        GradientPaint bgGradient = new GradientPaint(
                x, y, new Color(20, 20, 30, 180),
                x, y + height, new Color(5, 5, 15, 200));
        g.setPaint(bgGradient);
        g.fillRoundRect(x, y, width, height, 12, 12);

        // Draw glowing border effect
        Color glowColor = new Color(operatorColor.getRed(), operatorColor.getGreen(),
                operatorColor.getBlue(), 100);
        g.setColor(glowColor);
        g.setStroke(new BasicStroke(3.0f));
        g.drawRoundRect(x - 1, y - 1, width + 2, height + 2, 14, 14);

        // Draw main border
        g.setColor(operatorColor);
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(x, y, width, height, 12, 12);
        g.setStroke(new BasicStroke(1.0f));

        // Draw operator name with enhanced styling
        g.setFont(new Font("Arial", Font.BOLD, 11));
        String operatorName = (operator instanceof Player) ? "PLAYER" : "BOT " + operatorIndex;

        // Text shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(operatorName, x + 9, y + 15);

        // Main text
        g.setColor(Color.WHITE);
        g.drawString(operatorName, x + 8, y + 14);

        // Draw miniature planet representations with wrapping support
        int miniPlanetSize = 12;
        int miniPlanetSpacing = 2;
        int planetsPerRow = 5; // Maximum planets per row
        int maxVisiblePlanets = 10; // Show up to 2 rows (5 x 2)

        int miniPlanetStartX = x + width - 90; // Moved further from edge for more spacing
        if (operator instanceof Player) {
            miniPlanetStartX += 11; // More space for player
        }
        int miniPlanetStartY = y + 10;
        int miniPlanetsToShow = Math.min(planetCount, maxVisiblePlanets);

        // Show representative planets as mini enhanced planets with wrapping
        for (int i = 0; i < miniPlanetsToShow && i < planets.size(); i++) {
            Planet planet = planets.get(i);

            // Calculate row and column for wrapping
            int row = i / planetsPerRow;
            int col = i % planetsPerRow;

            int miniX = miniPlanetStartX + (col * (miniPlanetSize + miniPlanetSpacing));
            int miniY = miniPlanetStartY + (row * (miniPlanetSize + miniPlanetSpacing + 1));

            renderMiniaturePlanet(g, miniX, miniY, miniPlanetSize, operatorColor, planet);
        }

        // If there are more planets than we can show, display overflow indicator
        if (planetCount > maxVisiblePlanets) {
            g.setColor(operatorColor);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            String overflowText = "+" + (planetCount - maxVisiblePlanets);
            int textWidth = g.getFontMetrics().stringWidth(overflowText);
            int overflowX = x + width - textWidth - 8; // Adjusted for new spacing
            int overflowY = miniPlanetStartY + 25; // Position below wrapped planets

            // Background for overflow text
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect(overflowX - 2, overflowY - 9, textWidth + 4, 12, 4, 4);

            // Overflow text
            g.setColor(Color.WHITE);
            g.drawString(overflowText, overflowX, overflowY);
        }

        // Render ability effect indicators horizontally at the bottom
        renderHorizontalAbilityEffects(g, operator, x + 8, y + height - 12, width - 16);
    }

    private void renderMiniaturePlanet(Graphics2D g, int x, int y, int size, Color teamColor, Planet planet) {
        int halfSize = size / 2;

        // Create a mini version of the enhanced planet rendering
        Shape oldClip = g.getClip();
        g.setClip(new Ellipse2D.Double(x - halfSize, y - halfSize, size, size));

        // Generate base color similar to full planet rendering
        Color baseColor = generateMiniPlanetBaseColor(planet.getFeatures());

        // Draw planet shadow/base
        g.setColor(Color.BLACK);
        g.fillOval(x - halfSize, y - halfSize, size, size);

        // Draw planet body with gradient
        GradientPaint planetGradient = new GradientPaint(
                x - halfSize / 3, y - halfSize / 3, baseColor.brighter(),
                x + halfSize / 2, y + halfSize / 2, baseColor.darker());
        g.setPaint(planetGradient);
        g.fillOval(x - halfSize, y - halfSize, size, size);

        // Add surface texture (simplified)
        g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 40));
        for (int i = 0; i < 3; i++) {
            int textureX = x - halfSize / 2 + (i * size / 4);
            int textureY = y - halfSize / 2 + ((i % 2) * size / 3);
            g.fillOval(textureX, textureY, size / 4, size / 6);
        }

        // Draw lighting highlight (mini version)
        g.setColor(new Color(255, 255, 255, 60));
        g.fillOval(x - halfSize + 1, y - halfSize + 1, size / 3, size / 3);

        // Team color overlay (subtle)
        g.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 60));
        g.fillOval(x - halfSize, y - halfSize, size, size);

        g.setClip(oldClip);

        // Planet outline
        g.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 180));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x - halfSize, y - halfSize, size, size);
        g.setStroke(new BasicStroke(1.0f));

        // Health indicator (tiny bar under planet)
        if (planet != null) {
            int healthBarWidth = size;
            int healthBarHeight = 2;
            int healthBarX = x - healthBarWidth / 2;
            int healthBarY = y + halfSize + 1;

            // Background
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

            // Health
            g.setColor(Color.RED);
            g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
            g.setColor(Color.GREEN);
            double healthPercent = planet.getHealth() / (double) planet.getMaxHealth();
            int healthWidth = (int) (healthPercent * healthBarWidth);
            g.fillRect(healthBarX, healthBarY, healthWidth, healthBarHeight);
        }
    }

    private Color generateMiniPlanetBaseColor(home.game.planets.PlanetFeatures features) {
        if (features == null) {
            return new Color(120, 100, 80);
        }

        // Use features to generate consistent base color (simplified version of full
        // planet logic)
        Color surfaceTexture = features.getSurfaceTexture();
        if (surfaceTexture != null) {
            return surfaceTexture;
        }

        // Default rocky planet color
        return new Color(120, 100, 80);
    }

    private void renderHorizontalAbilityEffects(Graphics2D g, Operator operator, int x, int y, int availableWidth) {
        // Use the comprehensive EffectsArtist to render ability effects for all
        // operators
        effectsArtist.renderAbilityEffectsOnIndicator(g, operator, x, y);
    }
}
