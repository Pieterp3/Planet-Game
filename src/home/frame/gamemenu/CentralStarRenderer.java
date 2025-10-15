package home.frame.gamemenu;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import home.game.GameConstants;

public class CentralStarRenderer {

    /**
     * Renders a high-quality central star that planets orbit around
     */
    public void renderCentralStar(Graphics2D g) {
        int centerX = GameConstants.getGameWidth() / 2;
        int centerY = GameConstants.getGameHeight() / 2;
        int starSize = 36; // Slightly larger for better visibility

        // Enable high-quality rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        long currentTime = System.currentTimeMillis();

        // Multiple animation phases for complex motion
        double primaryPulse = (currentTime % 5000) / 5000.0 * 2 * Math.PI; // 5-second primary cycle
        double secondaryPulse = (currentTime % 7500) / 7500.0 * 2 * Math.PI; // 7.5-second secondary cycle
        double coronaPulse = (currentTime % 3200) / 3200.0 * 2 * Math.PI; // Corona animation
        double flarePhase = (currentTime % 12000) / 12000.0 * 2 * Math.PI; // Solar flare cycle

        float glowIntensity = (float) (0.4 + 0.15 * Math.sin(primaryPulse) + 0.05 * Math.sin(secondaryPulse));

        // Directional offset to simulate sun moving left (glow trails to the right)
        int glowOffsetX = (int) (8 + 4 * Math.sin(primaryPulse * 0.5));

        // Draw deep space background glow (largest, most subtle) with directional bias
        for (int i = 12; i > 8; i--) {
            float layerPhase = (float) (primaryPulse + i * 0.08);
            float layerScale = (float) (0.9 + 0.15 * Math.sin(layerPhase));
            int glowSize = (int) ((starSize + i * 12) * layerScale);

            // Reduced alpha for less dramatic effect
            float alpha = (0.015f + 0.01f / i) * glowIntensity;

            // Directional glow positioning (offset more to the left for outer layers -
            // pointing effect)
            int layerOffsetX = -(glowOffsetX + (i - 8));

            // Deep orange-red outer glow
            int red = 255;
            int green = Math.max(60, (int) (180 - i * 8));
            int blue = Math.max(0, (int) (30 - i * 2));

            g.setColor(new Color(red, green, blue, (int) (255 * alpha)));
            g.fillOval(centerX - glowSize / 2 + layerOffsetX, centerY - glowSize / 2, glowSize, glowSize);
        }

        // Draw stellar surface base with warm gradient (no black base)
        drawStellarSurfaceBase(g, centerX, centerY, starSize, primaryPulse);

        // Draw radial corona effects (solar wind simulation)
        drawSolarCorona(g, centerX, centerY, starSize, coronaPulse, glowIntensity);

        // Draw main stellar atmosphere layers with directional bias and blending
        // Set additive blending mode for smooth layer combination
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        for (int i = 8; i > 0; i--) {
            float layerPhase = (float) (primaryPulse + i * 0.12);
            double layerPulsePhase = primaryPulse + layerPhase;
            float layerScale = (float) (0.55 + 0.2 * Math.sin(layerPulsePhase));

            int baseGlowSize = starSize + (i * 8);
            int glowSize = (int) (baseGlowSize * layerScale);

            // Smoother alpha transition for better blending
            float baseAlpha = (float) (0.06f + (0.04f / Math.sqrt(i)));
            float alpha = baseAlpha * glowIntensity * (float) (0.7 + 0.15 * Math.sin(layerPulsePhase));

            // Directional offset decreases for inner layers (pointing to the left)
            int layerOffsetX = (glowOffsetX * i / 8);

            // More sophisticated color temperature gradient with smoother transitions
            float temperature = (float) (1.0 - i / 8.0); // 0 = outer, 1 = inner
            int red = 255;
            int green = (int) (255 * Math.pow(0.95 - i * 0.03, 0.9)); // Smoother exponential falloff
            int blue = (int) (Math.max(20, 130 * Math.pow(temperature, 2.0))); // Softer blackbody curve

            // Create wavy edges using a polygon with sine wave distortion
            int numPoints = 36; // More points for smoother wavy edges
            int[] xPoints = new int[numPoints];
            int[] yPoints = new int[numPoints];

            for (int p = 0; p < numPoints; p++) {
                double angle = (p * 2 * Math.PI) / numPoints;

                // Create wavy distortion with multiple sine frequencies
                double wavePhase = layerPulsePhase + angle * 3; // Base wave frequency
                double wavePhase2 = layerPulsePhase * 1.5 + angle * 7; // Higher frequency detail
                double wavePhase3 = layerPulsePhase * 0.7 + angle * 12; // Even finer detail

                // Combine multiple wave frequencies for complex edge pattern
                double waveOffset = 0.08 * Math.sin(wavePhase) +
                        0.04 * Math.sin(wavePhase2) +
                        0.02 * Math.sin(wavePhase3);

                // Apply wave distortion to radius (more pronounced for outer layers)
                double wavyRadius = (glowSize / 2.0) * (1.0 + waveOffset * (i / 4.0));

                xPoints[p] = (int) (centerX + layerOffsetX + Math.cos(angle) * wavyRadius);
                yPoints[p] = (int) (centerY + Math.sin(angle) * wavyRadius);
            }

            // Create radial gradient for smooth blending
            float gradientRadius = glowSize / 2.0f;
            GradientPaint layerGradient = new GradientPaint(
                    centerX + layerOffsetX, centerY,
                    new Color(red, green, blue, (int) (255 * alpha)),
                    centerX + layerOffsetX + gradientRadius * 0.8f, centerY + gradientRadius * 0.8f,
                    new Color(red, green, blue, 0) // Fade to transparent at edges
            );

            g.setPaint(layerGradient);
            g.fillPolygon(xPoints, yPoints, numPoints);
        }

        // Reset to solid color painting
        g.setPaint(null);

        // Draw solar flares (dramatic directional bursts)
        drawSolarFlares(g, centerX, centerY, starSize, flarePhase, glowIntensity);

        // Draw main stellar photosphere with realistic gradient
        int gradientRadius = starSize / 3;
        float photosphereIntensity = (float) (0.9 + 0.1 * Math.sin(primaryPulse * 1.3));

        // Multi-layer radial gradient for photosphere with subtle wavy edges
        for (int layer = 0; layer < 3; layer++) {
            float layerOffset = (float) (layer * 0.3);
            int offsetX = (int) (Math.cos(primaryPulse * 0.7 + layerOffset) * 2);
            int offsetY = (int) (Math.sin(primaryPulse * 0.7 + layerOffset) * 2);

            GradientPaint starGradient = new GradientPaint(
                    centerX - gradientRadius + offsetX, centerY - gradientRadius + offsetY,
                    new Color(255, 255, (int) (200 + 40 * photosphereIntensity), (int) (200 - layer * 50)), // Hot white
                                                                                                            // center
                    centerX + gradientRadius - offsetX, centerY + gradientRadius - offsetY,
                    new Color(255, (int) (140 + 60 * photosphereIntensity), (int) (20 + layer * 10),
                            (int) (180 - layer * 40))); // Orange-red edge

            // Create subtle wavy edges for photosphere
            int numCorePoints = 24; // Fewer points for smoother, less chaotic look
            int[] xCorePoints = new int[numCorePoints];
            int[] yCorePoints = new int[numCorePoints];

            for (int p = 0; p < numCorePoints; p++) {
                double angle = (p * 2 * Math.PI) / numCorePoints;

                // Much subtler wave pattern for the core - gentle undulation
                double coreWavePhase = primaryPulse * 0.5 + angle * 4 + layer * 0.5;
                double coreWavePhase2 = primaryPulse * 0.3 + angle * 8 + layer * 0.3;

                // Very gentle wave distortion - only 2-3% variation
                double coreWaveOffset = 0.02 * Math.sin(coreWavePhase) +
                        0.01 * Math.sin(coreWavePhase2);

                double coreRadius = (starSize / 2.0) * (1.0 + coreWaveOffset);

                xCorePoints[p] = (int) (centerX + Math.cos(angle) * coreRadius);
                yCorePoints[p] = (int) (centerY + Math.sin(angle) * coreRadius);
            }

            g.setPaint(starGradient);
            g.fillPolygon(xCorePoints, yCorePoints, numCorePoints);
        }

        // Draw chromosphere (middle atmospheric layer)
        drawChromosphere(g, centerX, centerY, starSize, secondaryPulse, photosphereIntensity);

        // Draw stellar core with realistic nuclear fusion glow
        drawStellarCore(g, centerX, centerY, starSize, primaryPulse, secondaryPulse);

        // Add surface granulation (convection cell simulation)
        drawSurfaceGranulation(g, centerX, centerY, starSize, currentTime);

        // Draw prominence loops (magnetic field lines)
        drawProminenceLoops(g, centerX, centerY, starSize, coronaPulse, glowIntensity);

        // Final stellar limb with subtle texture glow
        g.setColor(new Color(255, 200, 80, 60));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(centerX - starSize / 2, centerY - starSize / 2, starSize, starSize);

        // Reset rendering settings
        g.setStroke(new BasicStroke(1.0f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Draws the stellar surface base with realistic solar texture - primarily
     * yellow
     */
    private void drawStellarSurfaceBase(Graphics2D g, int centerX, int centerY, int starSize, double pulse) {
        // Create a bright yellow stellar base with subtle texture variations
        for (int layer = 3; layer > 0; layer--) {
            double layerPulse = pulse * (0.8 + layer * 0.1);
            float layerScale = (float) (0.95 + 0.1 * Math.sin(layerPulse));
            int layerSize = (int) ((starSize - layer * 2) * layerScale);

            // Bright yellow stellar colors - realistic sun colors
            int red = Math.max(240, 255 - layer * 5); // Keep red high for yellow
            int green = Math.max(220, 255 - layer * 10); // High green for yellow
            int blue = Math.max(60, 120 - layer * 15); // Lower blue for warm yellow
            float alpha = 0.8f + layer * 0.05f;

            g.setColor(new Color(red, green, blue, (int) (255 * alpha)));
            g.fillOval(centerX - layerSize / 2, centerY - layerSize / 2, layerSize, layerSize);
        }

        // Add subtle surface texture patterns
        drawStellarTexture(g, centerX, centerY, starSize, pulse);
    }

    /**
     * Draws realistic stellar surface texture patterns
     */
    private void drawStellarTexture(Graphics2D g, int centerX, int centerY, int starSize, double pulse) {
        // Create convection cell patterns across the surface
        int cellCount = 8;
        double texturePhase = pulse * 0.3;

        for (int i = 0; i < cellCount; i++) {
            for (int j = 0; j < cellCount; j++) {
                double angle = (i * Math.PI * 2 / cellCount) + texturePhase;
                double radius = (j + 1) * starSize / (cellCount * 3.0);

                if (radius < starSize * 0.4) { // Only draw within star bounds
                    int cellX = (int) (centerX + Math.cos(angle) * radius);
                    int cellY = (int) (centerY + Math.sin(angle) * radius);

                    double cellIntensity = 0.3 + 0.2 * Math.sin(pulse * 2 + i + j);
                    int cellSize = (int) (3 + 2 * cellIntensity);

                    // Bright convection cells
                    g.setColor(new Color(255, 220, 120, (int) (80 * cellIntensity)));
                    g.fillOval(cellX - cellSize / 2, cellY - cellSize / 2, cellSize, cellSize);
                }
            }
        }
    }

    /**
     * Draws the solar corona with streaming particle effects
     */
    private void drawSolarCorona(Graphics2D g, int centerX, int centerY, int starSize, double coronaPhase,
            float intensity) {
        // Draw radial corona streamers with directional bias
        for (int i = 0; i < 10; i++) { // Reduced from 12 to 10
            double angle = (i * Math.PI / 5) + coronaPhase * 0.1;
            float streamerIntensity = (float) (0.2 + 0.15 * Math.sin(coronaPhase + i * 0.5));

            // Bias streamers more toward the right (trailing effect)
            double angleBias = Math.cos(angle) * 0.3; // Stronger effect when pointing right
            int streamLength = (int) ((starSize * 2.2) * (0.7 + 0.3 * streamerIntensity + angleBias));
            int streamWidth = (int) (2 + 1.5 * streamerIntensity);

            // Calculate streamer endpoints
            int x1 = (int) (centerX + Math.cos(angle) * starSize * 0.7);
            int y1 = (int) (centerY + Math.sin(angle) * starSize * 0.7);
            int x2 = (int) (centerX + Math.cos(angle) * streamLength);
            int y2 = (int) (centerY + Math.sin(angle) * streamLength);

            // Draw streamer with reduced alpha
            float alpha = (0.05f + 0.025f * streamerIntensity) * intensity;
            g.setColor(new Color(255, 200, 100, (int) (255 * alpha)));
            g.setStroke(new BasicStroke(streamWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);
        }
        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Draws solar flares as subtle energy bursts
     */
    private void drawSolarFlares(Graphics2D g, int centerX, int centerY, int starSize, double flarePhase,
            float intensity) {
        // Show flares less frequently and with reduced threshold
        if (Math.sin(flarePhase) > 0.8) {
            for (int i = 0; i < 2; i++) { // Reduced from 3 to 2 flares
                double flareAngle = flarePhase + i * Math.PI;
                float flareIntensity = (float) Math.max(0, Math.sin(flarePhase + i) - 0.8) * 5.0f; // Scale to 0-1

                if (flareIntensity > 0) {
                    // Reduced flare size and width
                    int flareLength = (int) (starSize * (1.2 + flareIntensity * 0.5));
                    int flareWidth = (int) (2 + 2 * flareIntensity);

                    int startX = (int) (centerX + Math.cos(flareAngle) * starSize * 0.4);
                    int startY = (int) (centerY + Math.sin(flareAngle) * starSize * 0.4);
                    int endX = (int) (centerX + Math.cos(flareAngle) * flareLength);
                    int endY = (int) (centerY + Math.sin(flareAngle) * flareLength);

                    // Subtle white-hot flare core with reduced opacity
                    g.setColor(new Color(255, 255, 255, (int) (120 * flareIntensity * intensity)));
                    g.setStroke(new BasicStroke(flareWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(startX, startY, endX, endY);

                    // Subtle orange outer glow
                    g.setColor(new Color(255, 180, 80, (int) (80 * flareIntensity * intensity)));
                    g.setStroke(new BasicStroke(flareWidth + 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(startX, startY, endX, endY);
                }
            }
        }
        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Draws the chromosphere layer with spicule effects
     */
    private void drawChromosphere(Graphics2D g, int centerX, int centerY, int starSize, double phase, float intensity) {
        // Chromosphere appears as a thin reddish layer
        float chromosphereAlpha = (float) (0.15 * intensity * (0.8 + 0.2 * Math.sin(phase * 1.5)));
        g.setColor(new Color(255, 80, 80, (int) (255 * chromosphereAlpha)));

        int chromosphereSize = (int) (starSize * 1.1);
        g.fillOval(centerX - chromosphereSize / 2, centerY - chromosphereSize / 2, chromosphereSize, chromosphereSize);

        // Cut out the inner part to create a ring effect
        int innerSize = (int) (starSize * 0.95);
        g.fillOval(centerX - innerSize / 2, centerY - innerSize / 2, innerSize, innerSize);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
    }

    /**
     * Draws the stellar core with nuclear fusion effects
     */
    private void drawStellarCore(Graphics2D g, int centerX, int centerY, int starSize, double primaryPhase,
            double secondaryPhase) {
        // Multiple pulsing core layers
        for (int layer = 3; layer > 0; layer--) {
            double layerPhase = primaryPhase * (1.2 + layer * 0.3) + secondaryPhase * 0.5;
            float pulseFactor = (float) (0.7 + 0.3 * Math.sin(layerPhase));
            int coreSize = (int) (starSize / (2 + layer * 0.5) * pulseFactor);

            // Ultra-hot white core with hint of blue (like real stellar cores)
            int alpha = (int) (220 - layer * 40);
            Color coreColor;
            if (layer == 1) {
                coreColor = new Color(255, 255, 255, alpha); // Pure white center
            } else if (layer == 2) {
                coreColor = new Color(255, 255, 240, alpha); // Slight warm tint
            } else {
                coreColor = new Color(255, 245, 200, alpha); // Warmer outer core
            }

            // Create very subtle wavy edges for stellar core (minimal distortion)
            int numCorePoints = 16; // Even fewer points for smoother core
            int[] xCorePoints = new int[numCorePoints];
            int[] yCorePoints = new int[numCorePoints];

            for (int p = 0; p < numCorePoints; p++) {
                double angle = (p * 2 * Math.PI) / numCorePoints;

                // Extremely subtle wave pattern for the core - barely perceptible
                double coreWavePhase = layerPhase + angle * 3;

                // Minimal wave distortion - only 1% variation for inner stability
                double coreWaveOffset = 0.01 * Math.sin(coreWavePhase) * (layer / 3.0); // Less for inner layers

                double coreRadius = (coreSize / 2.0) * (1.0 + coreWaveOffset);

                xCorePoints[p] = (int) (centerX + Math.cos(angle) * coreRadius);
                yCorePoints[p] = (int) (centerY + Math.sin(angle) * coreRadius);
            }

            g.setColor(coreColor);
            g.fillPolygon(xCorePoints, yCorePoints, numCorePoints);
        }
    }

    /**
     * Draws surface granulation to simulate convection cells
     */
    private void drawSurfaceGranulation(Graphics2D g, int centerX, int centerY, int starSize, long currentTime) {
        // Small convection cells on the visible surface
        int granuleSize = 3;
        double granulePhase = (currentTime % 8000) / 8000.0 * 2 * Math.PI;

        for (int i = 0; i < 15; i++) {
            double angle = (i * Math.PI * 2 / 15) + granulePhase * 0.1;
            double distance = (starSize * 0.3) * (0.3 + 0.4 * Math.sin(granulePhase + i));

            int granuleX = (int) (centerX + Math.cos(angle) * distance);
            int granuleY = (int) (centerY + Math.sin(angle) * distance);

            float granuleIntensity = (float) (0.3 + 0.2 * Math.sin(granulePhase * 2 + i * 0.7));
            g.setColor(new Color(255, 255, 200, (int) (100 * granuleIntensity)));
            g.fillOval(granuleX - granuleSize / 2, granuleY - granuleSize / 2, granuleSize, granuleSize);
        }
    }

    /**
     * Draws prominence loops following magnetic field lines
     */
    private void drawProminenceLoops(Graphics2D g, int centerX, int centerY, int starSize, double phase,
            float intensity) {
        // Magnetic field line loops extending from the surface
        for (int i = 0; i < 6; i++) {
            double loopPhase = phase + i * Math.PI / 3;
            float loopIntensity = (float) Math.max(0, Math.sin(loopPhase + i * 0.5));

            if (loopIntensity > 0.3f) {
                double startAngle = i * Math.PI / 3 + phase * 0.1;
                int loopHeight = (int) (starSize * 0.4 * loopIntensity);

                int startX = (int) (centerX + Math.cos(startAngle) * starSize * 0.45);
                int startY = (int) (centerY + Math.sin(startAngle) * starSize * 0.45);

                // Draw curved prominence arc with solar colors
                g.setColor(new Color(255, 120, 80, (int) (180 * loopIntensity * intensity)));
                g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Simple arc approximation
                for (int j = 0; j < loopHeight; j += 2) {
                    double arcProgress = j / (double) loopHeight;
                    double arcCurve = Math.sin(arcProgress * Math.PI);

                    int arcX = (int) (startX + Math.cos(startAngle + Math.PI / 2) * j);
                    int arcY = (int) (startY + Math.sin(startAngle + Math.PI / 2) * j - arcCurve * 10);

                    // Add inner glow to prominence
                    g.setColor(new Color(255, 200, 150, (int) (120 * loopIntensity * intensity)));
                    g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(arcX, arcY, arcX + 1, arcY + 1);

                    // Outer prominence color
                    g.setColor(new Color(255, 120, 80, (int) (180 * loopIntensity * intensity)));
                    g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(arcX, arcY, arcX + 1, arcY + 1);
                }
            }
        }
        g.setStroke(new BasicStroke(1.0f));
    }

}
