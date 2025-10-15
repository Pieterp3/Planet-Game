package home.frame.gamemenu;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import home.game.GameConstants;
import home.game.VisualSettings;
import home.game.operators.Bot;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.planets.Crater;
import home.game.planets.Moon;
import home.game.planets.Planet;
import home.game.planets.PlanetFeatures;
import home.game.planets.PlanetType;
import home.game.planets.Ring;

public class PlanetArtist {

    private List<Planet> planets;
    private Planet hoveredPlanet = null, lastHoveredPlanet = null, clickedPlanet = null, selectedPlanet = null;

    private Map<Bot, Color> botColorMap;
    private List<Color> allBotColors;
    private CentralStarRenderer centralStarRenderer;
    private Color lastPlayerColor; // Track the last known player color

    public PlanetArtist() {
        botColorMap = new HashMap<>();
        allBotColors = new ArrayList<>();
        for (Color color : VisualSettings.getAvailablePlanetColors()) {
            allBotColors.add(color);
        }
        centralStarRenderer = new CentralStarRenderer();
    }

    public void setPlanets(List<Planet> planets) {
        this.planets = planets;
    }

    public void setHoveredPlanet(Planet hoveredPlanet) {
        this.hoveredPlanet = hoveredPlanet;
        if (hoveredPlanet != lastHoveredPlanet && hoveredPlanet != null) {
            lastHoveredPlanet = hoveredPlanet;
        }
    }

    public void setClickedPlanet(Planet clickedPlanet) {
        this.clickedPlanet = clickedPlanet;
    }

    public void setSelectedPlanet(Planet selectedPlanet) {
        this.selectedPlanet = selectedPlanet;
    }

    /**
     * Calculates the color distance between two colors in RGB space
     */
    private double calculateColorDistance(Color color1, Color color2) {
        int r1 = color1.getRed(), g1 = color1.getGreen(), b1 = color1.getBlue();
        int r2 = color2.getRed(), g2 = color2.getGreen(), b2 = color2.getBlue();

        // Calculate Euclidean distance in RGB space
        return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }

    /**
     * Checks if two colors are too similar based on visual perception
     * Uses both RGB distance and hue similarity for better detection
     */
    private boolean areColorsTooSimilar(Color color1, Color color2) {
        // Basic RGB distance check
        double rgbDistance = calculateColorDistance(color1, color2);
        if (rgbDistance < 150.0) { // Increased threshold for better separation
            return true;
        }

        // Additional check for colors in similar hue families
        // Convert to HSB to check hue similarity
        float[] hsb1 = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        // Check hue difference (hue is circular, so handle wraparound)
        float hueDiff = Math.abs(hsb1[0] - hsb2[0]);
        if (hueDiff > 0.5f) {
            hueDiff = 1.0f - hueDiff; // Handle circular nature of hue
        }

        // If hues are very similar (within 15% of the color wheel) and saturation is
        // similar
        if (hueDiff < 0.15f && Math.abs(hsb1[1] - hsb2[1]) < 0.3f) {
            return true;
        }

        return false;
    }

    /**
     * Gets a bot color that's sufficiently different from the player's color
     */
    private Color getAvailableBotColor() {
        Color playerColor = VisualSettings.getInstance().getPlayerPlanetColor();

        // First, try to find a color that's not too similar to player color or any
        // assigned bot color
        outer: for (Color candidateColor : allBotColors) {
            boolean isUsed = botColorMap.containsValue(candidateColor);
            if (!isUsed && !areColorsTooSimilar(playerColor, candidateColor)) {
                // Check against all assigned bot colors
                for (Color assignedBotColor : botColorMap.values()) {
                    if (areColorsTooSimilar(assignedBotColor, candidateColor)) {
                        continue outer;
                    }
                }
                return candidateColor;
            }
        }

        // If no unused dissimilar colors, find the most distant unused color from both
        // player and assigned bot colors
        Color bestColor = null;
        double maxDistance = 0;
        for (Color candidateColor : allBotColors) {
            boolean isUsed = botColorMap.containsValue(candidateColor);
            if (!isUsed) {
                // Calculate minimum distance to player and all assigned bot colors
                double minDistance = calculateColorDistance(playerColor, candidateColor);
                for (Color assignedBotColor : botColorMap.values()) {
                    double dist = calculateColorDistance(assignedBotColor, candidateColor);
                    if (dist < minDistance) {
                        minDistance = dist;
                    }
                }
                if (minDistance > maxDistance) {
                    maxDistance = minDistance;
                    bestColor = candidateColor;
                }
            }
        }

        // If all colors are used, cycle through them starting with most distant
        if (bestColor == null) {
            for (Color candidateColor : allBotColors) {
                double minDistance = calculateColorDistance(playerColor, candidateColor);
                for (Color assignedBotColor : botColorMap.values()) {
                    double dist = calculateColorDistance(assignedBotColor, candidateColor);
                    if (dist < minDistance) {
                        minDistance = dist;
                    }
                }
                if (minDistance > maxDistance) {
                    maxDistance = minDistance;
                    bestColor = candidateColor;
                }
            }
        }

        return bestColor != null ? bestColor : allBotColors.get(0);
    }

    /**
     * Clears bot color assignments - useful when player changes their color
     * to ensure bots get reassigned colors that don't conflict
     */
    public void resetBotColors() {
        botColorMap.clear();
    }

    /**
     * Checks if player color has changed and reassigns bot colors if needed
     */
    private void checkAndUpdateBotColors() {
        Color currentPlayerColor = VisualSettings.getInstance().getPlayerPlanetColor();

        // If this is the first check or player color has changed
        if (lastPlayerColor == null || !lastPlayerColor.equals(currentPlayerColor)) {
            lastPlayerColor = currentPlayerColor;

            // Check if any existing bot colors are too similar to the new player color
            boolean needsReassignment = false;

            for (Color botColor : botColorMap.values()) {
                if (areColorsTooSimilar(currentPlayerColor, botColor)) {
                    needsReassignment = true;
                    break;
                }
            }

            // If any bot colors are too similar, reassign all bot colors
            if (needsReassignment) {
                resetBotColors();
            }
        }
    }

    /**
     * Gets the color associated with an operator
     */
    public Color getOperatorColor(Operator operator) {
        // Check if player color changed and update bot colors if needed
        checkAndUpdateBotColors();

        if (operator instanceof Player) {
            return VisualSettings.getInstance().getPlayerPlanetColor();
        } else if (operator instanceof Bot) {
            Bot bot = (Bot) operator;
            if (!botColorMap.containsKey(bot)) {
                Color botColor = getAvailableBotColor();
                botColorMap.put(bot, botColor);
            }
            return botColorMap.get(bot);
        } else {
            return Color.LIGHT_GRAY;
        }
    }

    public void renderPlanets(Graphics2D g) {
        if (planets == null)
            return;

        // Draw central star first (behind planets)
        centralStarRenderer.renderCentralStar(g);

        // Sort planets by z-index (render back-to-front: lowest z-index first)
        // planets is already a snapshot created in GameMenu, so no need for additional
        // snapshotting
        List<Planet> sortedPlanets = new ArrayList<>(planets);
        sortedPlanets.sort((p1, p2) -> Double.compare(p1.getZIndex(), p2.getZIndex()));

        for (Planet planet : sortedPlanets) {
            int planetX = planet.getX();
            int planetY = planet.getY();
            // Apply depth scaling to planet size
            int planetSize = (int) (GameConstants.getPlanetSize() * planet.getDepthScale());
            // Planet color based on operator
            Color planetColor;
            if (planet.getOperator() instanceof Player) {
                planetColor = VisualSettings.getInstance().getPlayerPlanetColor();
            } else if (planet.getOperator() instanceof Bot) {
                // Check for color conflicts before assigning bot color
                checkAndUpdateBotColors();
                Bot bot = (Bot) planet.getOperator();
                if (!botColorMap.containsKey(bot)) {
                    Color botColor = getAvailableBotColor();
                    botColorMap.put(bot, botColor);
                }
                planetColor = botColorMap.get(bot);
            } else {
                planetColor = Color.LIGHT_GRAY;
            }
            drawEnhancedPlanet(g, planetX, planetY, planetSize, planetColor, planet.getRotationAngle(),
                    planet.getFeatures());

            // Draw planet type icon over the center
            drawPlanetTypeIcon(g, planetX, planetY, planetSize, planet.getType());

            // Draw targeting indicators at the top of the planet
            drawTargetingIndicators(g, planetX, planetY, planetSize, planet);

            // Draw hover highlight if this planet is being hovered
            if (planet == hoveredPlanet || planet == clickedPlanet) {
                drawHoverHighlight(g, planetX, planetY, planetSize);
            }
            // Draw health bar (scaled with planet)
            int healthBarWidth = planetSize;
            int healthBarHeight = Math.max(3, (int) (5 * planet.getDepthScale()));
            int healthBarX = planetX - healthBarWidth / 2;
            int healthBarY = planetY + planetSize / 2 + (int) (5 * planet.getDepthScale());
            g.setColor(Color.DARK_GRAY);
            g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
            g.setColor(Color.GREEN);
            int healthWidth = (int) ((planet.getHealth() / (double) planet.getMaxHealth()) * healthBarWidth);
            g.fillRect(healthBarX, healthBarY, healthWidth, healthBarHeight);
            g.setColor(Color.BLACK);
            g.drawRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

            // Draw lines to target planets with animated arrows (only when hovering)
            // Check visual settings for connection lines
            VisualSettings settings = VisualSettings.getInstance();
            if (!settings.isDisplayConnectionLines()) {
                continue; // Skip drawing connection lines if disabled
            }

            // Apply connection line opacity from settings
            int opacity = (int) (255 * settings.getConnectionLineOpacity());
            g.setColor(new Color(255, 255, 255, opacity));

            // Create a snapshot to avoid concurrent modification
            List<Planet> targetSnapshot;
            try {
                targetSnapshot = new ArrayList<>(planet.getTargets());
            } catch (Exception e) {
                // Skip drawing targets if there's any issue accessing them
                continue;
            }

            for (Planet target : targetSnapshot) {
                try {
                    // Only draw targeting lines if either the current planet or target is being
                    // hovered, clicked, or selected (for drag operations)
                    boolean shouldDrawLine = (hoveredPlanet == planet || hoveredPlanet == target) ||
                            (lastHoveredPlanet == planet || lastHoveredPlanet == target) ||
                            (clickedPlanet == planet || clickedPlanet == target) ||
                            (selectedPlanet == planet || selectedPlanet == target);

                    if (!shouldDrawLine) {
                        continue; // Skip drawing this targeting line
                    }

                    // Check if this is mutual targeting (both planets target each other)
                    List<Planet> mutualTargetSnapshot;
                    try {
                        mutualTargetSnapshot = new ArrayList<>(target.getTargets());
                    } catch (Exception e) {
                        // Skip mutual check if targets can't be accessed
                        continue;
                    }
                    boolean isMutualTargeting = mutualTargetSnapshot.contains(planet);

                    if (isMutualTargeting) {
                        // For mutual targeting, only draw from the planet with lower memory address
                        // to avoid drawing the same line twice
                        if (System.identityHashCode(planet) < System.identityHashCode(target)) {
                            drawMutualTargetingLine(g, planetX, planetY, target.getX(), target.getY(), planetColor,
                                    getPlanetColor(target));
                        }
                    } else {
                        // Normal one-way targeting
                        drawTargetingLineWithArrows(g, planetX, planetY, target.getX(), target.getY(), planetColor);
                    }
                } catch (Exception e) {
                    // Skip this target if there's any issue
                    continue;
                }
            }
        }
    }

    private void drawEnhancedPlanet(Graphics2D g, int x, int y, int size, Color teamColor, double rotationAngle,
            PlanetFeatures features) {
        int halfSize = size / 2;

        // Enable anti-aliasing for smoother graphics
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rings that are behind the planet first
        if (VisualSettings.getInstance().isDisplayEffects()) {
            for (Ring ring : features.getRings()) {
                drawRing(g, x, y, size, ring, rotationAngle, true); // Draw back portion
            }
        }

        // Draw moons that are behind the planet
        if (VisualSettings.getInstance().isDisplayPlanetMoons()) {
            for (Moon moon : features.getMoons()) {
                if (isMoonBehindPlanet(moon)) {
                    drawMoon(g, x, y, size, moon, true);
                }
            }
        }

        // Set clipping to planet circle to prevent halo effects
        Shape oldClip = g.getClip();
        g.setClip(new Ellipse2D.Double(x - halfSize, y - halfSize, size, size));

        // Create realistic planet base colors instead of team colors
        Color baseColor = generatePlanetBaseColor(features);

        // Draw deep space shadow (black base)
        g.setColor(Color.BLACK);
        g.fillOval(x - halfSize, y - halfSize, size, size);

        // Draw planet shadow/base (darker background)
        g.setColor(baseColor.darker().darker());
        g.fillOval(x - halfSize + 2, y - halfSize + 2, size - 4, size - 4);

        // Draw main planet body with realistic gradient
        GradientPaint gradient = new GradientPaint(
                x - halfSize / 3, y - halfSize / 3, baseColor.brighter(),
                x + halfSize / 2, y + halfSize / 2, baseColor.darker());
        g.setPaint(gradient);
        g.fillOval(x - halfSize, y - halfSize, size, size);

        // Draw enhanced surface texture with multiple layers
        drawSurfaceTexture(g, x, y, size, features, rotationAngle);

        // Draw craters with improved depth
        for (Crater crater : features.getCraters()) {
            drawEnhancedCrater(g, x, y, size, crater, rotationAngle, baseColor);
        }

        // Draw atmospheric effects
        if (VisualSettings.getInstance().isDisplayEffects()) {
            drawEnhancedAtmosphericEffect(g, x, y, size, baseColor, rotationAngle);
        }

        // Draw enhanced 3D lighting with multiple highlights
        drawEnhancedLighting(g, x, y, size, baseColor);

        // Draw team color overlay (transparent)
        g.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 80));
        g.fillOval(x - halfSize, y - halfSize, size, size);

        // Restore original clip
        g.setClip(oldClip);

        // Draw planet outline with enhanced style
        g.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 150));
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(x - halfSize, y - halfSize, size, size);
        g.setStroke(new BasicStroke(1.0f));

        // Draw rings that are in front of the planet
        if (VisualSettings.getInstance().isDisplayEffects()) {
            for (Ring ring : features.getRings()) {
                drawRing(g, x, y, size, ring, rotationAngle, false); // Draw front portion
            }
        }

        // Draw moons that are in front of the planet
        if (VisualSettings.getInstance().isDisplayPlanetMoons()) {
            for (Moon moon : features.getMoons()) {
                if (!isMoonBehindPlanet(moon)) {
                    drawMoon(g, x, y, size, moon, false);
                }
            }
        }

        // Reset rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private boolean isMoonBehindPlanet(Moon moon) {
        return Math.sin(moon.currentAngle) < 0;
    }

    private void drawMoon(Graphics2D g, int planetX, int planetY, int planetSize, Moon moon, boolean isBehind) {
        double moonX = planetX + Math.cos(moon.currentAngle) * moon.orbitRadius * planetSize / 2;
        double moonY = planetY + Math.sin(moon.currentAngle) * moon.orbitRadius * planetSize / 2 * 0.3;
        int moonSize = (int) (moon.size * planetSize);
        double sinValue = Math.sin(moon.currentAngle);
        float opacity = (float) (0.7f + 0.3f * sinValue);

        // Draw moon shadow
        g.setColor(moon.color.darker());
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g.fillOval((int) moonX - moonSize / 2, (int) moonY - moonSize / 2, moonSize, moonSize);

        // Draw moon surface
        g.setColor(moon.color);
        g.fillOval((int) moonX - moonSize / 2 + 1, (int) moonY - moonSize / 2 + 1, moonSize - 2, moonSize - 2);

        // Draw moon outline
        g.setColor(Color.BLACK);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity * 0.8f));
        g.drawOval((int) moonX - moonSize / 2, (int) moonY - moonSize / 2, moonSize, moonSize);

        // Reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Draws a targeting line with animated arrows pointing from source to target
     */
    private void drawTargetingLineWithArrows(Graphics2D g, int x1, int y1, int x2, int y2, Color lineColor) {
        // Draw the main line
        g.drawLine(x1, y1, x2, y2);

        // Calculate line length and direction
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lineLength = Math.sqrt(dx * dx + dy * dy);

        if (lineLength < 20)
            return; // Don't draw arrows on very short lines

        // Normalize direction vector
        double unitX = dx / lineLength;
        double unitY = dy / lineLength;

        // Arrow properties
        int arrowSize = 8;
        double arrowSpacing = 30.0; // Distance between arrows
        long currentTime = System.currentTimeMillis();
        double animationOffset = (currentTime / 50.0) % arrowSpacing; // Animation speed

        // Draw multiple arrows along the line
        for (double distance = animationOffset; distance < lineLength - 20; distance += arrowSpacing) {
            // Calculate arrow position
            double arrowX = x1 + unitX * distance;
            double arrowY = y1 + unitY * distance;

            // Calculate arrow opacity based on distance (fade near end)
            double fadeDistance = Math.min(distance, lineLength - distance);
            float opacity = (float) Math.min(1.0, fadeDistance / 50.0);

            if (opacity > 0.1f) {
                // Set arrow color with opacity
                g.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(),
                        (int) (255 * opacity * 0.6f)));

                // Draw arrowhead
                drawArrowHead(g, arrowX, arrowY, unitX, unitY, arrowSize);
            }
        }
    }

    /**
     * Draws an arrowhead at the specified position pointing in the given direction
     */
    private void drawArrowHead(Graphics2D g, double x, double y, double dirX, double dirY, int size) {
        // Calculate perpendicular vector for arrow wings
        double perpX = -dirY;
        double perpY = dirX;

        // Arrow tip point
        int tipX = (int) (x + dirX * size);
        int tipY = (int) (y + dirY * size);

        // Arrow wing points
        int wing1X = (int) (x - dirX * size * 0.5 + perpX * size * 0.5);
        int wing1Y = (int) (y - dirY * size * 0.5 + perpY * size * 0.5);
        int wing2X = (int) (x - dirX * size * 0.5 - perpX * size * 0.5);
        int wing2Y = (int) (y - dirY * size * 0.5 - perpY * size * 0.5);

        // Draw filled arrowhead
        int[] xPoints = { tipX, wing1X, wing2X };
        int[] yPoints = { tipY, wing1Y, wing2Y };
        g.fillPolygon(xPoints, yPoints, 3);

        // Draw arrow outline for better visibility
        g.setColor(new Color(0, 0, 0, 100));
        g.drawPolygon(xPoints, yPoints, 3);
    }

    /**
     * Draws a hover highlight effect around a planet
     */
    private void drawHoverHighlight(Graphics2D g, int x, int y, int size) {
        int highlightSize = size + 8; // Slightly larger than the planet
        int highlightHalfSize = highlightSize / 2;

        // Enable anti-aliasing for smooth highlight
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw pulsating glow effect
        long currentTime = System.currentTimeMillis();
        double pulsePhase = (currentTime % 1000) / 1000.0 * 2 * Math.PI; // Complete cycle every second
        float pulseAlpha = (float) (0.3 + 0.2 * Math.sin(pulsePhase)); // Alpha between 0.1 and 0.5

        // Outer glow
        g.setColor(new Color(255, 255, 255, (int) (255 * pulseAlpha * 0.5f)));
        g.fillOval(x - highlightHalfSize - 4, y - highlightHalfSize - 4, highlightSize + 8, highlightSize + 8);

        // Inner glow
        g.setColor(new Color(255, 255, 255, (int) (255 * pulseAlpha)));
        g.fillOval(x - highlightHalfSize, y - highlightHalfSize, highlightSize, highlightSize);

        // Highlight ring
        g.setColor(new Color(255, 255, 255, (int) (255 * (pulseAlpha + 0.3f))));
        g.drawOval(x - highlightHalfSize, y - highlightHalfSize, highlightSize, highlightSize);

        // Reset rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Draws type-specific icons over the center of planets
     */
    private void drawPlanetTypeIcon(Graphics2D g, int planetX, int planetY, int planetSize, PlanetType planetType) {
        // Enable anti-aliasing for smooth icons
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Icon size should scale with planet size but have a reasonable minimum/maximum
        int iconSize = Math.max(12, Math.min(32, planetSize / 3));

        // Set icon color to white with slight transparency for good visibility
        g.setColor(new Color(255, 255, 255, 220));

        switch (planetType) {
            case ATTACK:
                // Draw sword/attack icon - crossed lines forming an X with a handle
                drawAttackIcon(g, planetX, planetY, iconSize);
                break;
            case DEFENCE:
                // Draw shield icon - rounded rectangle with a cross pattern
                drawDefenceIcon(g, planetX, planetY, iconSize);
                break;
            case SPEED:
                // Draw speed icon - lightning bolt or arrow
                drawSpeedIcon(g, planetX, planetY, iconSize);
                break;
            default:
                // default/unknown type, do nothing
        }

        // Reset rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Draws targeting indicators showing current targets vs max targets
     */
    private void drawTargetingIndicators(Graphics2D g, int planetX, int planetY, int planetSize, Planet planet) {
        if (planet.getOperator() == null)
            return; // No indicators for neutral planets
        int maxTargets = planet.getMaxTargets();
        int currentTargets = planet.getTargets().size();

        if (maxTargets <= 0)
            return; // No targeting indicators if no targets possible

        // Enable anti-aliasing for smooth circles
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate indicator properties
        int indicatorRadius = Math.max(3, (int) (4 * planet.getDepthScale())); // Scale with planet depth
        int spacing = indicatorRadius + 4; // Space between indicators
        int totalWidth = maxTargets * (indicatorRadius * 2) + (maxTargets - 1) * 2; // Total width of all indicators

        // Position indicators at the top of the planet
        int startX = planetX - totalWidth / 2;
        int startY = planetY - planetSize / 2 - indicatorRadius - 8; // Above the planet

        // Draw each indicator
        for (int i = 0; i < maxTargets; i++) {
            int indicatorX = startX + i * spacing + indicatorRadius;
            int indicatorY = startY;

            boolean isFilled = i < currentTargets;

            if (isFilled) {
                // Draw filled circle with soft glow for used target slots
                // Create glow effect
                g.setColor(new Color(255, 255, 255, 60));
                g.fillOval(indicatorX - indicatorRadius - 1, indicatorY - indicatorRadius - 1,
                        (indicatorRadius + 1) * 2, (indicatorRadius + 1) * 2);

                // Draw main filled circle
                g.setColor(Color.WHITE);
                g.fillOval(indicatorX - indicatorRadius, indicatorY - indicatorRadius,
                        indicatorRadius * 2, indicatorRadius * 2);
            } else {
                // Draw empty circle with soft glow for unused target slots
                // Create glow effect
                g.setColor(new Color(255, 255, 255, 40));
                g.fillOval(indicatorX - indicatorRadius - 1, indicatorY - indicatorRadius - 1,
                        (indicatorRadius + 1) * 2, (indicatorRadius + 1) * 2);

                // Draw main empty circle (outline only)
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(indicatorX - indicatorRadius, indicatorY - indicatorRadius,
                        indicatorRadius * 2, indicatorRadius * 2);
            }
        }

        // Reset rendering hints and stroke
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Draws an attack icon (sword/crossed swords)
     */
    private void drawAttackIcon(Graphics2D g, int x, int y, int size) {
        int halfSize = size / 2;

        // Draw sword blade (diagonal line from top-left to bottom-right)
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x - halfSize + 4, y - halfSize + 4, x + halfSize - 4, y + halfSize - 4);

        // Draw sword crossguard (horizontal line)
        g.drawLine(x - halfSize / 2, y - halfSize / 2, x + halfSize / 2, y + halfSize / 2);

        // Draw sword handle (small circle at the bottom)
        g.fillOval(x + halfSize - 6, y + halfSize - 6, 4, 4);

        // Reset stroke
        g.setStroke(new BasicStroke(1));
    }

    /**
     * Draws a defence icon (shield)
     */
    private void drawDefenceIcon(Graphics2D g, int x, int y, int size) {
        int halfSize = size / 2;

        // Draw shield outline (rounded rectangle)
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(x - halfSize + 2, y - halfSize + 2, size - 4, size - 4, 6, 6);

        // Draw cross pattern inside shield
        g.drawLine(x, y - halfSize + 4, x, y + halfSize - 4); // Vertical line
        g.drawLine(x - halfSize + 4, y, x + halfSize - 4, y); // Horizontal line

        // Reset stroke
        g.setStroke(new BasicStroke(1));
    }

    /**
     * Draws a speed icon (lightning bolt)
     */
    private void drawSpeedIcon(Graphics2D g, int x, int y, int size) {
        int halfSize = size / 2;

        // Create lightning bolt shape using polygon
        int[] xPoints = {
                x - halfSize / 2, // Top left
                x + halfSize / 3, // Middle right
                x - halfSize / 4, // Middle left (inward)
                x + halfSize / 2 // Bottom right
        };
        int[] yPoints = {
                y - halfSize, // Top
                y - halfSize / 4, // Upper middle
                y + halfSize / 4, // Lower middle
                y + halfSize // Bottom
        };

        // Fill the lightning bolt
        g.fillPolygon(xPoints, yPoints, 4);

        // Draw outline for better definition
        g.setColor(new Color(0, 0, 0, 100));
        g.drawPolygon(xPoints, yPoints, 4);

        // Reset color
        g.setColor(new Color(255, 255, 255, 220));
    }

    /**
     * Gets the color for a planet based on its operator
     */
    private Color getPlanetColor(Planet planet) {
        if (planet.getOperator() instanceof Player) {
            return VisualSettings.getInstance().getPlayerPlanetColor();
        } else if (planet.getOperator() instanceof Bot) {
            // Check for color conflicts before assigning bot color
            checkAndUpdateBotColors();
            Bot bot = (Bot) planet.getOperator();
            if (!botColorMap.containsKey(bot)) {
                Color botColor = getAvailableBotColor();
                botColorMap.put(bot, botColor);
            }
            return botColorMap.get(bot);
        } else {
            return Color.LIGHT_GRAY;
        }
    }

    /**
     * Draws a mutual targeting line that meets in the middle with arrows pointing
     * in both directions
     */
    private void drawMutualTargetingLine(Graphics2D g, int x1, int y1, int x2, int y2, Color color1, Color color2) {
        // Calculate the midpoint
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;

        // Calculate line properties
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lineLength = Math.sqrt(dx * dx + dy * dy);

        // Only draw if line is long enough
        if (lineLength < 20)
            return;

        // Normalize direction vector
        double unitX = dx / lineLength;
        double unitY = dy / lineLength;

        // Draw the complete line first
        g.setColor(new Color(255, 255, 255, 100));
        g.drawLine(x1, y1, x2, y2);

        // Arrow properties
        int arrowSize = 8;
        double arrowSpacing = 30.0;
        long currentTime = System.currentTimeMillis();
        double animationOffset = (currentTime / 50.0) % arrowSpacing;

        // Draw arrows on first half (pointing toward middle) with color1
        for (double distance = animationOffset; distance < lineLength / 2 - 10; distance += arrowSpacing) {
            double arrowX = x1 + unitX * distance;
            double arrowY = y1 + unitY * distance;

            double fadeDistance = Math.min(distance, lineLength / 2 - distance);
            float opacity = (float) Math.min(1.0, fadeDistance / 50.0);

            if (opacity > 0.1f) {
                g.setColor(new Color(color1.getRed(), color1.getGreen(), color1.getBlue(),
                        (int) (255 * opacity * 0.8f)));
                drawArrowHead(g, arrowX, arrowY, unitX, unitY, arrowSize);
            }
        }

        // Draw arrows on second half (pointing toward center from planet 2) with color2
        double halfLength = lineLength / 2;
        for (double distance = halfLength + (animationOffset % arrowSpacing); distance < lineLength
                - 10; distance += arrowSpacing) {
            double arrowX = x1 + unitX * distance;
            double arrowY = y1 + unitY * distance;

            double fadeDistance = Math.min(distance - halfLength, lineLength - distance);
            float opacity = (float) Math.min(1.0, fadeDistance / 50.0);

            if (opacity > 0.1f) {
                g.setColor(new Color(color2.getRed(), color2.getGreen(), color2.getBlue(),
                        (int) (255 * opacity * 0.8f)));
                // For second half, arrows should point in opposite direction (toward center)
                drawArrowHead(g, arrowX, arrowY, -unitX, -unitY, arrowSize);
            }
        }

        // Draw a small connector circle at the midpoint to show the connection
        g.setColor(new Color(255, 255, 255, 180));
        int connectorSize = 6;
        g.fillOval(midX - connectorSize / 2, midY - connectorSize / 2, connectorSize, connectorSize);
        g.setColor(new Color(0, 0, 0, 100));
        g.drawOval(midX - connectorSize / 2, midY - connectorSize / 2, connectorSize, connectorSize);
    }

    /**
     * Generates a realistic base color for a planet based on its features
     */
    private Color generatePlanetBaseColor(PlanetFeatures features) {
        Color surfaceTexture = features.getSurfaceTexture();
        // Create more varied planet colors based on surface texture
        int red = Math.max(50, Math.min(200, surfaceTexture.getRed() + 50));
        int green = Math.max(40, Math.min(180, surfaceTexture.getGreen() + 30));
        int blue = Math.max(30, Math.min(160, surfaceTexture.getBlue() + 20));
        return new Color(red, green, blue);
    }

    /**
     * Draws enhanced surface texture with multiple layers for more detail
     */
    private void drawSurfaceTexture(Graphics2D g, int x, int y, int size, PlanetFeatures features,
            double rotationAngle) {
        int halfSize = size / 2;
        Color surfaceTexture = features.getSurfaceTexture();

        // Layer 1: Base texture (darker)
        g.setColor(surfaceTexture.darker());
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g.fillOval(x - halfSize + 3, y - halfSize + 3, size - 6, size - 6);

        // Layer 2: Mid texture with rotation effect
        int offsetX = (int) (Math.cos(rotationAngle) * 2);
        int offsetY = (int) (Math.sin(rotationAngle) * 2);
        g.setColor(surfaceTexture);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.fillOval(x - halfSize + 4 + offsetX, y - halfSize + 4 + offsetY, size - 8, size - 8);

        // Layer 3: Surface highlights
        g.setColor(surfaceTexture.brighter());
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g.fillOval(x - halfSize + 6, y - halfSize + 6, size - 12, size - 12);

        // Reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Draws enhanced craters with better depth and detail
     */
    private void drawEnhancedCrater(Graphics2D g, int planetX, int planetY, int planetSize,
            Crater crater, double rotationAngle, Color planetColor) {
        // Calculate crater position with rotation
        double rotatedX = crater.relativeX * Math.cos(rotationAngle) - crater.relativeY * Math.sin(rotationAngle);
        double rotatedY = crater.relativeX * Math.sin(rotationAngle) + crater.relativeY * Math.cos(rotationAngle);

        // Only draw craters on the visible side (with some wrap-around)
        if (rotatedX > -0.7) {
            int craterX = (int) (planetX + rotatedX * planetSize / 2);
            int craterY = (int) (planetY + rotatedY * planetSize / 2);
            int craterSize = (int) (crater.size * planetSize);

            // Draw crater shadow (deeper)
            g.setColor(Color.BLACK);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) crater.depth * 0.6f));
            g.fillOval(craterX - craterSize / 2, craterY - craterSize / 2, craterSize, craterSize);

            // Draw crater rim (lighter)
            g.setColor(planetColor.brighter());
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) crater.depth * 0.4f));
            g.drawOval(craterX - craterSize / 2, craterY - craterSize / 2, craterSize, craterSize);

            // Draw inner crater details
            int innerSize = craterSize / 3;
            g.setColor(planetColor.darker().darker());
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) crater.depth * 0.8f));
            g.fillOval(craterX - innerSize / 2, craterY - innerSize / 2, innerSize, innerSize);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    /**
     * Draws enhanced atmospheric effects with more layers
     */
    private void drawEnhancedAtmosphericEffect(Graphics2D g, int x, int y, int size, Color color,
            double rotationAngle) {
        // Multiple atmospheric layers for depth
        for (int layer = 0; layer < 3; layer++) {
            float alpha = 0.06f - (layer * 0.02f);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(color.brighter());

            // Create shifting highlights that suggest rotation and atmosphere
            double layerOffset = rotationAngle + (layer * Math.PI / 3);
            int shimmerX = (int) (Math.cos(layerOffset) * size * 0.1);
            int shimmerY = (int) (Math.sin(layerOffset) * size * 0.05);

            int effectSize = size / (4 + layer);
            g.fillOval(x - effectSize / 2 + shimmerX, y - effectSize / 2 + shimmerY,
                    effectSize, effectSize / 2);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Draws enhanced lighting with multiple highlight layers
     */
    private void drawEnhancedLighting(Graphics2D g, int x, int y, int size, Color baseColor) {
        int halfSize = size / 2;

        // Main highlight (larger, more subtle)
        int ellipseHeight = size / 4;
        g.setColor(baseColor.brighter().brighter());
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g.fillOval(x - halfSize + 8, y - halfSize + 5, size - 16, ellipseHeight);

        // Secondary highlight (smaller, brighter)
        ellipseHeight = size / 6;
        g.setColor(Color.WHITE);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        g.fillOval(x - halfSize + 12, y - halfSize + 8, size - 24, ellipseHeight);

        // Specular highlight (very small, very bright)
        ellipseHeight = size / 8;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.fillOval(x - halfSize + 15, y - halfSize + 10, size - 30, ellipseHeight);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Draws planetary rings with proper depth and perspective
     */
    private void drawRing(Graphics2D g, int x, int y, int size, Ring ring, double rotationAngle, boolean isBehind) {
        // Calculate ring dimensions
        int innerRadius = (int) (ring.innerRadius * size / 2);
        int outerRadius = (int) (ring.outerRadius * size / 2);

        // Calculate animated tilt - slow seesaw oscillation over time
        long currentTime = System.currentTimeMillis();
        double tiltCycle = (currentTime % 15000) / 15000.0 * 2 * Math.PI; // 15-second cycle
        double animatedTilt = ring.getTilt() + Math.sin(tiltCycle) * 0.15; // Base tilt + gentle seesaw oscillation

        // Create ring perspective (elliptical height)
        int ellipseHeight = (int) ((outerRadius - innerRadius) * 0.3); // Base ellipse height

        // Adjust opacity based on whether it's behind or in front
        float opacity = (float) ring.opacity;
        if (isBehind) {
            opacity *= 0.6f; // Dimmer when behind planet
        }

        // Enable anti-aliasing for smooth rings
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Save the current graphics transform
        AffineTransform originalTransform = g.getTransform();

        // Apply seesaw tilt rotation around the planet center
        g.rotate(animatedTilt, x, y);

        // Draw ring shadow (if behind)
        if (isBehind) {
            g.setColor(new Color(0, 0, 0, (int) (100 * opacity)));
            g.fillOval(x - outerRadius + 2, y - ellipseHeight / 2 + 2,
                    outerRadius * 2, ellipseHeight);
            // Cut out inner part
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            g.fillOval(x - innerRadius + 2, y - ellipseHeight / 2 + 2,
                    innerRadius * 2, ellipseHeight);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }

        // Draw main ring with seesaw tilt rotation
        Color ringColor = ring.color;
        g.setColor(new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(),
                (int) (255 * opacity)));

        // Draw outer ring (now rotated)
        g.fillOval(x - outerRadius, y - ellipseHeight / 2, outerRadius * 2, ellipseHeight);

        // Cut out inner part to create ring effect
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g.fillOval(x - innerRadius, y - ellipseHeight / 2, innerRadius * 2, ellipseHeight);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        // Add ring detail lines
        if (!isBehind) {
            g.setColor(new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(),
                    (int) (100 * opacity)));
            g.drawOval(x - outerRadius, y - ellipseHeight / 2, outerRadius * 2, ellipseHeight);
            g.drawOval(x - innerRadius, y - ellipseHeight / 2, innerRadius * 2, ellipseHeight);
        }

        // Restore the original graphics transform
        g.setTransform(originalTransform);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

}
