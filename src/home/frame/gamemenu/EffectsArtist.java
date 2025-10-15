package home.frame.gamemenu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import home.game.Game;
import home.game.abilities.BlackHole;
import home.game.abilities.AbilityManager;
import home.game.abilities.AbilityType;
import home.game.operators.Bot;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.operators.player.PlayerData;
import home.game.planets.Planet;

/**
 * Handles rendering of all ability and effect visuals in the game
 */
public class EffectsArtist {

    // Healing animation tracking
    private Map<Planet, Long> healingAnimations = new HashMap<>();
    private static final long HEALING_ANIMATION_DURATION = 2000; // 2 seconds

    private Game game;

    public EffectsArtist(Game game) {
        this.game = game;
    }

    /**
     * Main method to render all ability effects on planets
     */
    public void renderAbilityEffects(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long currentTime = System.currentTimeMillis();
        AbilityManager abilityManager = game.getAbilityManager();

        // Check if Answered Prayers was just activated to start healing animations
        if (abilityManager.wasHealingJustUsed()) {
            // Start healing animation for all player planets that aren't already animating
            for (Planet planet : game.getPlanets()) {
                if (planet.getOperator() instanceof Player && !healingAnimations.containsKey(planet)) {
                    healingAnimations.put(planet, currentTime);
                }
            }
        }

        // Render effects for each planet
        for (Planet planet : game.getPlanets()) {
            int planetX = planet.getX();
            int planetY = planet.getY();
            int planetRadius = (int) planet.getActualRadius();

            boolean isPlayerPlanet = planet.getOperator() instanceof Player;
            boolean isEmptyPlanet = planet.getOperator() == null;

            // Check if this planet should show freeze effect (affected by enemy freeze)
            boolean showFreeze = false;
            if (!isEmptyPlanet) {
                // Check if player freeze affects this planet (if it's not a player planet)
                if (abilityManager.isFreezeActive() && !isPlayerPlanet) {
                    showFreeze = true;
                }
                // Check if any bot freeze affects this planet (if it's not that bot's planet)
                if (!showFreeze) {
                    for (Planet otherPlanet : game.getPlanets()) {
                        if (otherPlanet.getOperator() instanceof Bot) {
                            Bot bot = (Bot) otherPlanet.getOperator();
                            if (bot.isBotFreezeActive() && planet.getOperator() != bot) {
                                showFreeze = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Freeze effect - blue crystalline overlay on affected planets
            if (showFreeze) {
                renderFreezeEffect(g, planetX, planetY, planetRadius, currentTime);
            }

            // Orbital freeze effect - totally encased in ice
            if (abilityManager.isPlanetOrbitallyFrozen(planet)) {
                renderOrbitalFreezeEffect(g, planetX, planetY, planetRadius, currentTime);
            }

            // Check if this planet is cursed by ANY operator
            boolean planetCursed = false;
            if (!isEmptyPlanet) {
                // Check player curse
                planetCursed = abilityManager.isPlanetCursed(planet);

                // Check bot curses
                if (!planetCursed) {
                    for (Planet otherPlanet : game.getPlanets()) {
                        if (otherPlanet.getOperator() instanceof Bot) {
                            Bot bot = (Bot) otherPlanet.getOperator();
                            if (bot.isBotCurseActive() && planet.getOperator() != bot) {
                                planetCursed = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Curse effect - purple smoke on cursed planets
            if (planetCursed) {
                renderCurseEffect(g, planetX, planetY, planetRadius, currentTime);
            }

            // Effects that only show on player-owned planets
            if (isPlayerPlanet) {
                // Shield effect - blue energy bubble
                if (abilityManager.isShieldActive()) {
                    renderShieldEffect(g, planetX, planetY, planetRadius, currentTime);
                }

                // Factory Hype effect - electric sparks
                if (abilityManager.isFactoryHypeActive()) {
                    renderFactoryHypeEffect(g, planetX, planetY, planetRadius, currentTime);
                }

                // Improved Factories effect - mechanical gears
                if (abilityManager.isImprovedFactoriesActive()) {
                    renderImprovedFactoriesEffect(g, planetX, planetY, planetRadius, currentTime);
                }

                // Planetary Flame effect - rotating flame towers
                if (abilityManager.isPlanetaryFlameActive()) {
                    renderPlanetaryFlameEffect(g, planetX, planetY, planetRadius, currentTime);
                }
            }

            // Effects for bot-owned planets
            if (planet.getOperator() instanceof Bot) {
                Bot bot = (Bot) planet.getOperator();

                // Bot shield effect - same as player but with bot color overlay
                if (bot.isBotShieldActive()) {
                    renderBotShieldEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot factory hype effect - same as player but with bot color overlay
                if (bot.isBotFactoryHypeActive()) {
                    renderBotFactoryHypeEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot improved factories effect - same as player but with bot color overlay
                if (bot.isBotImprovedFactoriesActive()) {
                    renderBotImprovedFactoriesEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot black hole effect - same as player but with bot color overlay
                if (bot.isBotBlackHoleActive()) {
                    renderBotBlackHoleEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot planetary flame effect - same as player but with bot color overlay
                if (bot.isBotPlanetaryFlameActive()) {
                    renderBotPlanetaryFlameEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot freeze effect - same as player but with bot color overlay
                if (bot.isBotFreezeActive()) {
                    renderBotFreezeEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot missile barrage effect - same as player but with bot color overlay
                if (bot.isBotMissileBarrageActive()) {
                    renderBotMissileBarrageEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot answered prayers effect - same as player but with bot color overlay
                if (bot.isBotAnsweredPrayersActive()) {
                    renderBotAnsweredPrayersEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot curse effect - same as player but with bot color overlay
                if (bot.isBotCurseActive()) {
                    renderBotCurseEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot unstoppable ships effect - same as player but with bot color overlay
                if (bot.isBotUnstoppableShipsActive()) {
                    renderBotUnstoppableShipsEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }

                // Bot orbital freeze effect - same as player but with bot color overlay
                if (bot.isBotOrbitalFreezeActive()) {
                    renderBotOrbitalFreezeEffect(g, planetX, planetY, planetRadius, currentTime, bot);
                }
            }

            // Check if this planet is infected by ANY operator
            boolean planetInfected = false;
            if (!isEmptyPlanet) {
                // Check player infection
                planetInfected = abilityManager.isPlanetInfected(planet);

                // Check bot infections using AbilityManager
                if (!planetInfected) {
                    for (Planet otherPlanet : game.getPlanets()) {
                        if (otherPlanet.getOperator() instanceof Bot) {
                            Bot bot = (Bot) otherPlanet.getOperator();
                            if (abilityManager.isOperatorPlanetInfected(bot, planet)) {
                                planetInfected = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Planetary Infection effect - dark green infection on infected planets
            if (planetInfected) {
                renderPlanetaryInfectionEffect(g, planetX, planetY, planetRadius, currentTime);
            }

            // Check if any player planets have unstoppable ships active for visual effects
            if (isPlayerPlanet) {
                // Healing animation - green plus signs floating up
                if (healingAnimations.containsKey(planet)) {
                    long animationStart = healingAnimations.get(planet);
                    long animationAge = currentTime - animationStart;

                    if (animationAge < HEALING_ANIMATION_DURATION) {
                        renderHealingAnimation(g, planetX, planetY, planetRadius, animationAge);
                    } else {
                        // Remove completed animation
                        healingAnimations.remove(planet);
                    }
                }
            }
        }

        // Render black holes
        for (BlackHole blackHole : abilityManager.getBlackHoles()) {
            renderBlackHole(g, blackHole, currentTime);
        }
    }

    private void renderFreezeEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Create pulsing ice crystals
        float alpha = 0.3f + 0.2f * (float) Math.sin(time * 0.005);
        g.setColor(new Color(150, 200, 255, (int) (alpha * 255)));

        // Draw ice crystal overlay
        int crystalSize = radius + 5;
        g.drawOval(x - crystalSize, y - crystalSize, crystalSize * 2, crystalSize * 2);

        // Draw ice spikes
        int numSpikes = 8;
        for (int i = 0; i < numSpikes; i++) {
            double angle = (i * 2 * Math.PI) / numSpikes;
            int spikeLength = 8;
            int startX = x + (int) (radius * Math.cos(angle));
            int startY = y + (int) (radius * Math.sin(angle));
            int endX = x + (int) ((radius + spikeLength) * Math.cos(angle));
            int endY = y + (int) ((radius + spikeLength) * Math.sin(angle));

            g.drawLine(startX, startY, endX, endY);
        }
    }

    private void renderOrbitalFreezeEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Orbital freeze effect - completely encases the planet in ice

        // Main ice ball - solid ice blue with transparency
        float alpha = 0.8f + 0.1f * (float) Math.sin(time * 0.003); // Slow pulsing
        g.setColor(new Color(180, 220, 255, (int) (alpha * 255)));

        // Draw solid ice sphere over the entire planet
        int iceRadius = radius + 8; // Slightly larger than planet
        g.fillOval(x - iceRadius, y - iceRadius, iceRadius * 2, iceRadius * 2);

        // Ice crystal facet lines for definition
        g.setColor(new Color(120, 180, 220, (int) (alpha * 200)));
        g.setStroke(new BasicStroke(2.0f));

        // Vertical crystal lines
        g.drawLine(x - iceRadius / 3, y - iceRadius + 2, x - iceRadius / 3, y + iceRadius - 2);
        g.drawLine(x + iceRadius / 3, y - iceRadius + 2, x + iceRadius / 3, y + iceRadius - 2);

        // Horizontal crystal lines
        g.drawLine(x - iceRadius + 2, y - iceRadius / 3, x + iceRadius - 2, y - iceRadius / 3);
        g.drawLine(x - iceRadius + 2, y + iceRadius / 3, x + iceRadius - 2, y + iceRadius / 3);

        // Diagonal crystal lines for more detail
        g.drawLine(x - iceRadius / 2, y - iceRadius / 2, x + iceRadius / 2, y + iceRadius / 2);
        g.drawLine(x - iceRadius / 2, y + iceRadius / 2, x + iceRadius / 2, y - iceRadius / 2);

        // Ice highlights - bright white spots that shimmer
        float highlightAlpha = 0.4f + 0.3f * (float) Math.sin(time * 0.007 + 1.0);
        g.setColor(new Color(255, 255, 255, (int) (highlightAlpha * 255)));
        g.fillOval(x - iceRadius / 2, y - iceRadius / 2, 4, 4);
        g.fillOval(x + iceRadius / 3, y - iceRadius / 3, 3, 3);
        g.fillOval(x - iceRadius / 4, y + iceRadius / 4, 3, 3);

        // Outer ice rim with subtle glow
        g.setColor(new Color(200, 240, 255, (int) (alpha * 150)));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x - iceRadius - 2, y - iceRadius - 2, (iceRadius + 2) * 2, (iceRadius + 2) * 2);

        // Reset stroke
        g.setStroke(new BasicStroke(1.0f));
    }

    private void renderShieldEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Create shimmering energy shield
        float alpha = 0.4f + 0.3f * (float) Math.sin(time * 0.008);
        g.setColor(new Color(100, 150, 255, (int) (alpha * 255)));

        int shieldRadius = radius + 8;

        // Draw main shield bubble
        g.drawOval(x - shieldRadius, y - shieldRadius, shieldRadius * 2, shieldRadius * 2);

        // Draw hexagonal pattern
        int hexSize = shieldRadius / 2;
        for (int i = 0; i < 6; i++) {
            double angle1 = (i * Math.PI) / 3;
            double angle2 = ((i + 1) * Math.PI) / 3;

            int x1 = x + (int) (hexSize * Math.cos(angle1));
            int y1 = y + (int) (hexSize * Math.sin(angle1));
            int x2 = x + (int) (hexSize * Math.cos(angle2));
            int y2 = y + (int) (hexSize * Math.sin(angle2));

            g.drawLine(x1, y1, x2, y2);
        }
    }

    private void renderFactoryHypeEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Create electric lightning effect
        g.setColor(new Color(255, 255, 100, 200));

        // Draw random lightning bolts around planet
        Random rand = new Random(time / 100); // Seed with time for animation
        int numBolts = 4;

        for (int i = 0; i < numBolts; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            int startRadius = radius + 3;
            int endRadius = radius + 12;

            int startX = x + (int) (startRadius * Math.cos(angle));
            int startY = y + (int) (startRadius * Math.sin(angle));
            int endX = x + (int) (endRadius * Math.cos(angle));
            int endY = y + (int) (endRadius * Math.sin(angle));

            // Add jagged lightning effect
            int midX = (startX + endX) / 2 + rand.nextInt(6) - 3;
            int midY = (startY + endY) / 2 + rand.nextInt(6) - 3;

            g.drawLine(startX, startY, midX, midY);
            g.drawLine(midX, midY, endX, endY);
        }
    }

    private void renderImprovedFactoriesEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Create rotating gear effect
        g.setColor(new Color(150, 150, 150, 180));

        double rotation = (time * 0.002) % (2 * Math.PI);

        // Draw gear teeth around planet
        int gearRadius = radius + 6;
        int numTeeth = 12;

        for (int i = 0; i < numTeeth; i++) {
            double angle = rotation + (i * 2 * Math.PI) / numTeeth;

            // Inner point (valley)
            int innerX = x + (int) (gearRadius * Math.cos(angle));
            int innerY = y + (int) (gearRadius * Math.sin(angle));

            // Outer point (tooth)
            double toothAngle = angle + (Math.PI / numTeeth);
            int outerX = x + (int) ((gearRadius + 4) * Math.cos(toothAngle));
            int outerY = y + (int) ((gearRadius + 4) * Math.sin(toothAngle));

            // Next valley
            double nextAngle = rotation + ((i + 1) * 2 * Math.PI) / numTeeth;
            int nextInnerX = x + (int) (gearRadius * Math.cos(nextAngle));
            int nextInnerY = y + (int) (gearRadius * Math.sin(nextAngle));

            // Draw tooth
            g.drawLine(innerX, innerY, outerX, outerY);
            g.drawLine(outerX, outerY, nextInnerX, nextInnerY);
        }
    }

    private void renderHealingAnimation(Graphics2D g, int x, int y, int radius, long animationAge) {
        // Create floating healing crosses
        g.setColor(new Color(100, 255, 100, 200));

        float progress = (float) animationAge / HEALING_ANIMATION_DURATION;
        int numCrosses = 3;

        for (int i = 0; i < numCrosses; i++) {
            // Calculate position - crosses float upward and spread out
            double angle = (i * 2 * Math.PI) / numCrosses;
            float floatHeight = progress * 30; // Float up 30 pixels
            float spread = progress * 15; // Spread out 15 pixels

            int crossX = x + (int) (spread * Math.cos(angle));
            int crossY = y - radius - (int) floatHeight;

            // Fade out as animation progresses
            float alpha = 1.0f - progress;
            g.setColor(new Color(100, 255, 100, (int) (alpha * 200)));

            // Draw cross (+ symbol)
            int crossSize = 4;
            g.drawLine(crossX - crossSize, crossY, crossX + crossSize, crossY);
            g.drawLine(crossX, crossY - crossSize, crossX, crossY + crossSize);
        }
    }

    private void renderCurseEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Create swirling purple smoke effect
        g.setColor(new Color(150, 50, 200, 150));

        // Draw wavy smoke particles rising
        for (int i = 0; i < 8; i++) {
            double angle = (i * 2 * Math.PI / 8) + (time * 0.001);
            int smokeRadius = radius + 8 + (i % 3) * 4;

            double waveOffset = Math.sin(time * 0.003 + i) * 3;
            int smokeX = x + (int) ((smokeRadius + waveOffset) * Math.cos(angle));
            int smokeY = y + (int) ((smokeRadius + waveOffset) * Math.sin(angle));

            // Draw small smoke particles
            g.fillOval(smokeX - 2, smokeY - 2, 4, 4);

            // Draw wavy trails
            float alpha = 0.3f + 0.2f * (float) Math.sin(time * 0.004 + i);
            g.setColor(new Color(150, 50, 200, (int) (alpha * 255)));

            for (int j = 1; j < 6; j++) {
                double trailAngle = angle + (j * 0.1);
                int trailRadius = smokeRadius - (j * 2);
                int trailX = x + (int) (trailRadius * Math.cos(trailAngle));
                int trailY = y + (int) (trailRadius * Math.sin(trailAngle));
                g.drawLine(smokeX, smokeY, trailX, trailY);
                smokeX = trailX;
                smokeY = trailY;
            }
        }
    }

    private void renderBlackHole(Graphics2D g, BlackHole blackHole, long time) {
        int x = (int) blackHole.x;
        int y = (int) blackHole.y;
        int horizon = blackHole.eventHorizon / 10;

        // Draw event horizon
        g.setColor(Color.BLACK);
        g.fillOval(x - horizon / 2, y - horizon / 2, horizon, horizon);

        // Draw accretion disk with animated rotation
        double rotation = blackHole.rotationAngle;

        // Draw multiple layers of the accretion disk
        for (int layer = 0; layer < 3; layer++) {
            int diskRadius = horizon / 2 + 10 + (layer * 8);
            float alpha = 0.8f - (layer * 0.2f);

            // Color gradient from hot center to cooler edges
            Color diskColor = switch (layer) {
                case 0 -> new Color(255, 200, 100, (int) (alpha * 255));
                case 1 -> new Color(255, 150, 50, (int) (alpha * 255));
                default -> new Color(200, 100, 50, (int) (alpha * 255));
            };

            g.setColor(diskColor);

            // Draw spiral pattern
            int numSpirals = 20;
            for (int i = 0; i < numSpirals; i++) {
                double spiralAngle = rotation + (i * 2 * Math.PI / numSpirals);
                double spiralRadius = diskRadius * (0.7 + 0.3 * Math.sin(spiralAngle * 3));

                int spiralX = x + (int) (spiralRadius * Math.cos(spiralAngle));
                int spiralY = y + (int) (spiralRadius * Math.sin(spiralAngle));

                // Draw small particles in spiral
                g.fillOval(spiralX - 2, spiralY - 2, 4, 4);

                // Connect to create spiral lines
                if (i > 0) {
                    double prevAngle = rotation + ((i - 1) * 2 * Math.PI / numSpirals);
                    double prevRadius = diskRadius * (0.7 + 0.3 * Math.sin(prevAngle * 3));
                    int prevX = x + (int) (prevRadius * Math.cos(prevAngle));
                    int prevY = y + (int) (prevRadius * Math.sin(prevAngle));
                    g.drawLine(prevX, prevY, spiralX, spiralY);
                }
            }
        }

        // Draw gravitational lensing effect
        g.setColor(new Color(255, 255, 255, 50));
        g.drawOval(x - horizon / 2 - 5, y - horizon / 2 - 5, horizon + 10, horizon + 10);
    }

    private void renderPlanetaryFlameEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Get flame power from ability level
        int flamePower = PlayerData.getInstance().getAbilityPower(AbilityType.PLANETARY_FLAME);
        int flameLength = Math.min(80, flamePower / 10); // Cap flame length

        // Calculate rotation for flame towers
        double rotation = time * 0.002; // Continuous rotation

        // Draw two flame towers rotating around the planet
        for (int tower = 0; tower < 2; tower++) {
            double towerAngle = rotation + (tower * Math.PI); // 180 degrees apart

            // Base position of flame tower
            int towerX = x + (int) ((radius + 5) * Math.cos(towerAngle));
            int towerY = y + (int) ((radius + 5) * Math.sin(towerAngle));

            // Draw flame extending outward
            for (int i = 0; i < flameLength; i += 3) {
                double currentRadius = radius + 5 + i;
                double flameX = x + currentRadius * Math.cos(towerAngle);
                double flameY = y + currentRadius * Math.sin(towerAngle);

                // Flame intensity decreases with distance
                float intensity = 1.0f - (i / (float) flameLength);

                // Core flame (bright yellow/orange)
                if (intensity > 0.7f) {
                    g.setColor(new Color(255, 255, 100, (int) (intensity * 255)));
                } else if (intensity > 0.4f) {
                    g.setColor(new Color(255, 150, 50, (int) (intensity * 255)));
                } else {
                    g.setColor(new Color(200, 50, 50, (int) (intensity * 255)));
                }

                // Add flame flicker
                int flicker = (int) (2 * Math.sin(time * 0.01 + i * 0.1));
                int flameSize = 6 + flicker;

                g.fillOval((int) flameX - flameSize / 2, (int) flameY - flameSize / 2, flameSize, flameSize);

                // Add some random flame particles
                if (i % 6 == 0) {
                    double particleAngle = towerAngle + (Math.sin(time * 0.005 + i) * 0.2);
                    int particleX = (int) (flameX + 3 * Math.cos(particleAngle));
                    int particleY = (int) (flameY + 3 * Math.sin(particleAngle));

                    g.setColor(new Color(255, 200, 100, (int) (intensity * 200)));
                    g.fillOval(particleX - 2, particleY - 2, 4, 4);
                }
            }

            // Draw flame tower base
            g.setColor(new Color(100, 50, 50, 200));
            g.fillOval(towerX - 3, towerY - 3, 6, 6);
        }
    }

    private void renderPlanetaryInfectionEffect(Graphics2D g, int x, int y, int radius, long time) {
        // Dark green infection spreading across the planet surface
        float alpha = 0.4f + 0.2f * (float) Math.sin(time * 0.006);

        // Main infection overlay - pulsing dark green
        g.setColor(new Color(50, 120, 50, (int) (alpha * 180)));
        g.fillOval(x - radius - 2, y - radius - 2, (radius + 2) * 2, (radius + 2) * 2);

        // Infection spikes/tendrils extending outward
        int numSpikes = 12;
        for (int i = 0; i < numSpikes; i++) {
            double angle = (i * 2 * Math.PI) / numSpikes + time * 0.001; // Slow rotation
            int spikeLength = 6 + (int) (3 * Math.sin(time * 0.008 + i));

            int startX = x + (int) (radius * Math.cos(angle));
            int startY = y + (int) (radius * Math.sin(angle));
            int endX = x + (int) ((radius + spikeLength) * Math.cos(angle));
            int endY = y + (int) ((radius + spikeLength) * Math.sin(angle));

            g.setColor(new Color(30, 80, 30, (int) (alpha * 200)));
            g.setStroke(new BasicStroke(2));
            g.drawLine(startX, startY, endX, endY);

            // Small infection dots at the tips
            g.fillOval(endX - 1, endY - 1, 2, 2);
        }

        // Restore default stroke
        g.setStroke(new BasicStroke(1));

        // Pulsing infection core
        int coreSize = 4 + (int) (2 * Math.sin(time * 0.01));
        g.setColor(new Color(20, 60, 20, (int) (alpha * 255)));
        g.fillOval(x - coreSize / 2, y - coreSize / 2, coreSize, coreSize);
    }

    // Bot ability visual effects (similar to player effects)

    private void renderBotShieldEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Render the standard shield effect (same as player)
        renderShieldEffect(g, x, y, radius, time);
    }

    private void renderBotFactoryHypeEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Render the standard factory hype effect (same as player)
        renderFactoryHypeEffect(g, x, y, radius, time);
    }

    private void renderBotImprovedFactoriesEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Render the standard improved factories effect (same as player)
        renderImprovedFactoriesEffect(g, x, y, radius, time);
    }

    private void renderBotBlackHoleEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Bot black holes are rendered as field effects in the main black hole
        // rendering loop
        // This method exists for consistency but doesn't need to render anything here
        // since bot black holes appear as actual BlackHole objects in the field
    }

    private void renderBotPlanetaryFlameEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Render the standard planetary flame effect (same as player)
        renderPlanetaryFlameEffect(g, x, y, radius, time);
    }

    private void renderBotFreezeEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Bot freeze effects are rendered globally in the main freeze rendering logic
        // This method exists for consistency but freeze effects are handled at the
        // planet level
    }

    private void renderBotMissileBarrageEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Bot missile barrage - missiles are created as actual projectiles in the field
        // Visual effect shows enhanced offensive capability with pulsing rings
        g.setColor(new Color(255, 100, 100, 150));

        // Draw expanding rings to show missile launch activity
        for (int i = 0; i < 3; i++) {
            int ringRadius = radius + 5 + (i * 8) + (int) (4 * Math.sin(time * 0.008 + i));
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - ringRadius, y - ringRadius, ringRadius * 2, ringRadius * 2);
        }

        g.setStroke(new BasicStroke(1));
    }

    private void renderBotAnsweredPrayersEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Bot answered prayers - create healing effect with gentle green healing aura
        g.setColor(new Color(100, 255, 100, 150));

        // Draw healing rings
        for (int i = 0; i < 3; i++) {
            int ringRadius = radius + 8 + (i * 6) + (int) (3 * Math.sin(time * 0.008 + i));
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - ringRadius, y - ringRadius, ringRadius * 2, ringRadius * 2);
        }

        g.setStroke(new BasicStroke(1));
    }

    private void renderBotCurseEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Bot curse effects are rendered globally in the main curse rendering logic
        // This method exists for consistency but curse effects are handled at the
        // planet level
    }

    private void renderBotUnstoppableShipsEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Bot unstoppable ships - create enhanced ship production visual with bright
        // pulsing effect
        float alpha = 0.6f + 0.4f * (float) Math.sin(time * 0.01);
        g.setColor(new Color(255, 255, 100, (int) (alpha * 200)));

        // Draw enhancement rings
        for (int i = 0; i < 2; i++) {
            int ringRadius = radius + 10 + (i * 6) + (int) (3 * Math.sin(time * 0.012 + i));
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - ringRadius, y - ringRadius, ringRadius * 2, ringRadius * 2);
        }

        g.setStroke(new BasicStroke(1));
    }

    private void renderBotOrbitalFreezeEffect(Graphics2D g, int x, int y, int radius, long time, Bot bot) {
        // Bot orbital freeze effects are rendered globally in the main orbital freeze
        // rendering logic
        // This method exists for consistency but orbital freeze effects are handled at
        // the planet level
    }

    // Mini effect rendering methods for operator indicators

    public void renderMiniShieldEffect(Graphics2D g, int x, int y, int size, long time) {
        float alpha = 0.6f + 0.4f * (float) Math.sin(time * 0.008);
        g.setColor(new Color(100, 150, 255, (int) (alpha * 255)));
        g.drawOval(x - size / 2, y - size / 2, size, size);

        // Mini hexagon
        int hexSize = size / 3;
        for (int i = 0; i < 6; i++) {
            double angle1 = (i * Math.PI) / 3;
            double angle2 = ((i + 1) * Math.PI) / 3;
            int x1 = x + (int) (hexSize * Math.cos(angle1));
            int y1 = y + (int) (hexSize * Math.sin(angle1));
            int x2 = x + (int) (hexSize * Math.cos(angle2));
            int y2 = y + (int) (hexSize * Math.sin(angle2));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public void renderMiniFactoryHypeEffect(Graphics2D g, int x, int y, int size, long time) {
        g.setColor(new Color(255, 255, 100, 200));

        // Simple lightning bolts
        for (int i = 0; i < 2; i++) {
            double angle = i * Math.PI;
            int x1 = x + (int) (size / 3 * Math.cos(angle));
            int y1 = y + (int) (size / 3 * Math.sin(angle));
            int x2 = x + (int) (size / 2 * Math.cos(angle));
            int y2 = y + (int) (size / 2 * Math.sin(angle));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public void renderMiniImprovedFactoriesEffect(Graphics2D g, int x, int y, int size, long time) {
        g.setColor(new Color(150, 150, 150, 180));
        double rotation = (time * 0.002) % (2 * Math.PI);

        // Mini gear teeth
        int numTeeth = 6;
        for (int i = 0; i < numTeeth; i++) {
            double angle = rotation + (i * 2 * Math.PI) / numTeeth;
            int x1 = x + (int) (size / 3 * Math.cos(angle));
            int y1 = y + (int) (size / 3 * Math.sin(angle));
            int x2 = x + (int) (size / 2 * Math.cos(angle));
            int y2 = y + (int) (size / 2 * Math.sin(angle));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public void renderMiniPlanetaryFlameEffect(Graphics2D g, int x, int y, int size, long time) {
        double rotation = time * 0.002;

        // Two mini flame towers
        for (int tower = 0; tower < 2; tower++) {
            double towerAngle = rotation + (tower * Math.PI);
            int flameX = x + (int) (size / 3 * Math.cos(towerAngle));
            int flameY = y + (int) (size / 3 * Math.sin(towerAngle));

            g.setColor(new Color(255, 150, 50, 200));
            g.fillOval(flameX - 2, flameY - 2, 4, 4);
        }
    }

    public void renderMiniFreezeEffect(Graphics2D g, int x, int y, int size, long time) {
        float alpha = 0.5f + 0.3f * (float) Math.sin(time * 0.005);
        g.setColor(new Color(150, 200, 255, (int) (alpha * 255)));

        // Ice crystal overlay
        g.drawOval(x - size / 2, y - size / 2, size, size);

        // Ice spikes
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI) / 2;
            int x1 = x + (int) (size / 3 * Math.cos(angle));
            int y1 = y + (int) (size / 3 * Math.sin(angle));
            int x2 = x + (int) (size / 2 * Math.cos(angle));
            int y2 = y + (int) (size / 2 * Math.sin(angle));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public void renderMiniCurseEffect(Graphics2D g, int x, int y, int size, long time) {
        // More visible purple curse indicator with better contrast
        float alpha = 0.8f + 0.2f * (float) Math.sin(time * 0.003);

        // Main curse circle background
        g.setColor(new Color(120, 0, 150, (int) (alpha * 200)));
        g.fillOval(x - size / 2, y - size / 2, size, size);

        // Bright purple border
        g.setColor(new Color(200, 100, 255, (int) (alpha * 255)));
        g.drawOval(x - size / 2, y - size / 2, size, size);

        // Swirling purple particles - brighter and larger
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2) + (time * 0.002);
            int smokeX = x + (int) (size / 3 * Math.cos(angle));
            int smokeY = y + (int) (size / 3 * Math.sin(angle));
            g.setColor(new Color(255, 150, 255, (int) (alpha * 220)));
            g.fillOval(smokeX - 1, smokeY - 1, 3, 3);
        }
    }

    public void renderMiniInfectionEffect(Graphics2D g, int x, int y, int size, long time) {
        float alpha = 0.7f + 0.3f * (float) Math.sin(time * 0.006);

        // Main infection circle background - brighter green
        g.setColor(new Color(80, 160, 80, (int) (alpha * 200)));
        g.fillOval(x - size / 2, y - size / 2, size, size);

        // Bright green border
        g.setColor(new Color(120, 255, 120, (int) (alpha * 255)));
        g.drawOval(x - size / 2, y - size / 2, size, size);

        // Infection spikes - brighter and thicker
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2) + time * 0.002;
            int spikeLength = size / 3;
            int x1 = x + (int) (size / 4 * Math.cos(angle));
            int y1 = y + (int) (size / 4 * Math.sin(angle));
            int x2 = x + (int) ((size / 4 + spikeLength) * Math.cos(angle));
            int y2 = y + (int) ((size / 4 + spikeLength) * Math.sin(angle));
            g.setColor(new Color(150, 255, 150, (int) (alpha * 255)));
            g.drawLine(x1, y1, x2, y2);
        }
        g.setStroke(new BasicStroke(1)); // Reset stroke

        // Infection core - brighter
        g.setColor(new Color(200, 255, 200, (int) (alpha * 255)));
        g.fillOval(x - 2, y - 2, 4, 4);
    }

    public void renderMiniUnstoppableShipsEffect(Graphics2D g, int x, int y, int size, long time) {
        float alpha = 0.7f + 0.3f * (float) Math.sin(time * 0.01);

        // Bright yellow/gold core representing unstoppable force
        g.setColor(new Color(255, 255, 150, (int) (alpha * 220)));
        g.fillOval(x - size / 2, y - size / 2, size, size);

        // Golden border
        g.setColor(new Color(255, 200, 0, (int) (alpha * 255)));
        g.setStroke(new BasicStroke(2));
        g.drawOval(x - size / 2, y - size / 2, size, size);
        g.setStroke(new BasicStroke(1)); // Reset stroke

        // Energy sparks radiating outward
        for (int i = 0; i < 4; i++) {
            double sparkAngle = (time * 0.005) + (i * Math.PI / 2);
            int sparkDistance = size / 2 + 3;

            int sparkX = x + (int) (sparkDistance * Math.cos(sparkAngle));
            int sparkY = y + (int) (sparkDistance * Math.sin(sparkAngle));

            g.setColor(new Color(255, 255, 100, (int) (alpha * 200)));
            g.fillOval(sparkX - 1, sparkY - 1, 2, 2);
        }
    }

    public void renderMiniBlackHoleEffect(Graphics2D g, int x, int y, int size, long time) {
        // Mini black hole with event horizon
        g.setColor(Color.BLACK);
        g.fillOval(x - size / 4, y - size / 4, size / 2, size / 2);

        // Mini accretion disk
        double rotation = time * 0.003;
        g.setColor(new Color(255, 150, 50, 180));

        // Draw mini spiral
        for (int i = 0; i < 4; i++) {
            double angle = rotation + (i * Math.PI / 2);
            int diskRadius = size / 3;
            int spiralX = x + (int) (diskRadius * Math.cos(angle));
            int spiralY = y + (int) (diskRadius * Math.sin(angle));
            g.fillOval(spiralX - 1, spiralY - 1, 2, 2);
        }
    }

    /**
     * Renders ability effect indicators on operator indicators in the UI
     */
    public void renderAbilityEffectsOnIndicator(Graphics2D g, Operator operator, int x, int y) {
        AbilityManager abilityManager = game.getAbilityManager();
        long currentTime = System.currentTimeMillis();

        // Small effect size for indicators (much smaller than on planets)
        int effectSize = 8;
        int effectSpacing = 3;
        int currentX = x; // Render horizontally

        // Check if this operator has planets affected by abilities
        boolean hasEffectedPlanets = false;
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() == operator) {
                hasEffectedPlanets = true;
                break;
            }
        }

        if (!hasEffectedPlanets) {
            return; // No planets owned by this operator
        }

        // For player operator
        if (operator instanceof Player) {
            // Shield effect
            if (abilityManager.isShieldActive()) {
                renderMiniShieldEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Factory Hype effect
            if (abilityManager.isFactoryHypeActive()) {
                renderMiniFactoryHypeEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Improved Factories effect
            if (abilityManager.isImprovedFactoriesActive()) {
                renderMiniImprovedFactoriesEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Planetary Flame effect
            if (abilityManager.isPlanetaryFlameActive()) {
                renderMiniPlanetaryFlameEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Black Hole effect
            if (!abilityManager.getBlackHoles().isEmpty()) {
                renderMiniBlackHoleEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Unstoppable Ships effect
            if (abilityManager.isUnstoppableShipsActive()) {
                renderMiniUnstoppableShipsEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }
        }

        // For bot operator
        if (operator instanceof Bot) {
            Bot bot = (Bot) operator;

            // Bot shield effect
            if (bot.isBotShieldActive()) {
                renderMiniShieldEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Bot factory hype effect
            if (bot.isBotFactoryHypeActive()) {
                renderMiniFactoryHypeEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Bot improved factories effect
            if (bot.isBotImprovedFactoriesActive()) {
                renderMiniImprovedFactoriesEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Bot planetary flame effect
            if (bot.isBotPlanetaryFlameActive()) {
                renderMiniPlanetaryFlameEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Bot freeze effect
            if (bot.isBotFreezeActive()) {
                renderMiniFreezeEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Bot black hole effect
            if (bot.isBotBlackHoleActive()) {
                renderMiniBlackHoleEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }

            // Bot unstoppable ships effect
            if (bot.isBotUnstoppableShipsActive()) {
                renderMiniUnstoppableShipsEffect(g, currentX, y, effectSize, currentTime);
                currentX += effectSize + effectSpacing;
            }
        }

        // Effects that can affect any operator's planets

        // Check for freeze effects (affecting this operator's planets)
        boolean affectedByFreeze = false;
        if (operator instanceof Player) {
            // Check if any bot freeze affects player planets
            for (Planet planet : game.getPlanets()) {
                if (planet.getOperator() instanceof Bot) {
                    Bot bot = (Bot) planet.getOperator();
                    if (bot.isBotFreezeActive()) {
                        affectedByFreeze = true;
                        break;
                    }
                }
            }
        } else if (operator instanceof Bot) {
            // Check if player freeze or other bot freeze affects this bot's planets
            if (abilityManager.isFreezeActive()) {
                affectedByFreeze = true;
            } else {
                for (Planet planet : game.getPlanets()) {
                    if (planet.getOperator() instanceof Bot && planet.getOperator() != operator) {
                        Bot otherBot = (Bot) planet.getOperator();
                        if (otherBot.isBotFreezeActive()) {
                            affectedByFreeze = true;
                            break;
                        }
                    }
                }
            }
        }

        if (affectedByFreeze) {
            renderMiniFreezeEffect(g, currentX, y, effectSize, currentTime);
            currentX += effectSize + effectSpacing;
        }

        // Check for curse effects - use comprehensive curse map
        boolean affectedByCurse = false;
        Map<Planet, Long> allCursedPlanets = abilityManager.getCursedPlanets();

        for (home.game.planets.Planet planet : game.getPlanets()) {
            if (planet.getOperator() == operator && allCursedPlanets.containsKey(planet)) {
                affectedByCurse = true;
                break;
            }
        }

        if (affectedByCurse) {
            renderMiniCurseEffect(g, currentX, y, effectSize, currentTime);
            currentX += effectSize + effectSpacing;
        }

        // Check for infection effects - use comprehensive infection map
        boolean affectedByInfection = false;
        Map<Planet, Long> allInfectedPlanets = abilityManager.getInfectedPlanets();

        for (home.game.planets.Planet planet : game.getPlanets()) {
            if (planet.getOperator() == operator && allInfectedPlanets.containsKey(planet)) {
                affectedByInfection = true;
                break;
            }
        }

        if (affectedByInfection) {
            renderMiniInfectionEffect(g, currentX, y, effectSize, currentTime);
            currentX += effectSize + effectSpacing;
        }
    }
}