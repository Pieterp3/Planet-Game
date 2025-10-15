package home.frame.gamemenu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

import home.game.Game;
import home.game.GameConstants;
import home.game.Projectile;
import home.game.Ship;
import home.game.VisualSettings;
import home.game.operators.player.Player;
import home.game.planets.PlanetType;

public class ShipArtist {

    private List<Ship> ships;
    private List<Projectile> projectiles;
    private PlanetArtist planetArtist; // For accessing operator colors
    private Game game; // For accessing ability manager

    public ShipArtist(PlanetArtist planetArtist) {
        this.planetArtist = planetArtist;
    }

    public void setShips(List<Ship> ships) {
        this.ships = ships;
    }

    public void setProjectiles(List<Projectile> projectiles) {
        this.projectiles = projectiles;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void renderShips(Graphics2D g) {
        if (ships == null)
            return;

        // Check visual settings for ships
        if (!VisualSettings.getInstance().isDisplayShips()) {
            return; // Skip rendering ships if disabled
        }

        // Enable anti-aliasing for smooth ship graphics
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // ships is already a snapshot created in GameMenu, so no need for additional
        // snapshotting
        for (Ship ship : ships) {
            try {
                int shipX = ship.getX();
                int shipY = ship.getY();
                double direction = ship.getDirection();

                // Determine ship color based on operator
                Color shipColor;
                if (ship.getOperator() instanceof Player) {
                    shipColor = VisualSettings.getInstance().getPlayerShipColor();
                } else {
                    // Use planet color with .lighter() for bot ships
                    Color baseColor = planetArtist.getOperatorColor(ship.getOperator());
                    shipColor = baseColor.brighter();
                }

                // Get planet type for ship design
                PlanetType planetType = ship.getOrigin() != null ? ship.getOrigin().getType() : PlanetType.STANDARD;
                drawSpaceship(g, shipX, shipY, direction, shipColor, ship.getHealth(), ship.getSpeed(), ship,
                        planetType);
            } catch (Exception e) {
                System.out.println("Error drawing ship: " + e.getMessage());
                continue;
            }
        }

        // Render projectiles
        renderProjectiles(g);

        // Reset rendering hints
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Draws a spaceship with thrusters pointing in the given direction
     */
    private void drawSpaceship(Graphics2D g, int x, int y, double direction, Color color, int health, double speed,
            Ship ship, PlanetType planetType) {
        // Calculate ship orientation vectors
        double cosDir = Math.cos(direction);
        double sinDir = Math.sin(direction);
        double perpCosDir = Math.cos(direction + Math.PI / 2);
        double perpSinDir = Math.sin(direction + Math.PI / 2);

        // Draw ship based on planet type
        switch (planetType) {
            case ATTACK:
                drawAttackShip(g, x, y, cosDir, sinDir, perpCosDir, perpSinDir, color);
                break;
            case DEFENCE:
                drawDefenceShip(g, x, y, cosDir, sinDir, perpCosDir, perpSinDir, color);
                break;
            case SPEED:
                drawSpeedShip(g, x, y, cosDir, sinDir, perpCosDir, perpSinDir, color);
                break;
            case STANDARD:
            default:
                drawStandardShip(g, x, y, cosDir, sinDir, perpCosDir, perpSinDir, color);
                break;
        }

        // Draw thrusters
        drawThrusters(g, x, y, direction, speed);

        // Draw cockpit/engine glow (universal for all ship types)
        g.setColor(Color.WHITE);
        int cockpitSize = 3;
        int cockpitX = (int) (x + cosDir * 4); // Standard cockpit position
        int cockpitY = (int) (y + sinDir * 4);
        g.fillOval(cockpitX - cockpitSize / 2, cockpitY - cockpitSize / 2, cockpitSize, cockpitSize);

        // Draw health indicator (small bar below ship)
        if (health > 0) {
            // drawHealthBar(g, x, y, health, 100); // Assuming max health is 100
        }

        // Draw unstoppable ships effect for player ships
        if (VisualSettings.getInstance().isDisplayEffects() && ship.getOperator() instanceof Player && game != null) {
            if (game.getAbilityManager().isUnstoppableShipsActive()) {
                drawUnstoppableShipsEffect(g, x, y, direction);
            }
        }
    }

    /**
     * Draws a standard triangle-shaped ship (original design)
     */
    private void drawStandardShip(Graphics2D g, int x, int y, double cosDir, double sinDir, double perpCosDir,
            double perpSinDir, Color color) {
        int shipLength = 16;
        int shipWidth = 8;

        // Ship body points (triangle-like shape)
        int[] shipBodyX = new int[4];
        int[] shipBodyY = new int[4];

        // Front tip of the ship
        shipBodyX[0] = (int) (x + cosDir * shipLength / 2);
        shipBodyY[0] = (int) (y + sinDir * shipLength / 2);

        // Right wing
        shipBodyX[1] = (int) (x - cosDir * shipLength / 4 + perpCosDir * shipWidth / 2);
        shipBodyY[1] = (int) (y - sinDir * shipLength / 4 + perpSinDir * shipWidth / 2);

        // Back center (thruster attachment point)
        shipBodyX[2] = (int) (x - cosDir * shipLength / 2);
        shipBodyY[2] = (int) (y - sinDir * shipLength / 2);

        // Left wing
        shipBodyX[3] = (int) (x - cosDir * shipLength / 4 - perpCosDir * shipWidth / 2);
        shipBodyY[3] = (int) (y - sinDir * shipLength / 4 - perpSinDir * shipWidth / 2);

        drawShipWithShadow(g, shipBodyX, shipBodyY, color);
    }

    /**
     * Draws an aggressive attack ship with angular spikes
     */
    private void drawAttackShip(Graphics2D g, int x, int y, double cosDir, double sinDir, double perpCosDir,
            double perpSinDir, Color color) {
        int shipLength = 18;
        int shipWidth = 6;

        // Ship body points (aggressive angular shape with spikes)
        int[] shipBodyX = new int[6];
        int[] shipBodyY = new int[6];

        // Front spike (extended and sharp)
        shipBodyX[0] = (int) (x + cosDir * shipLength / 2);
        shipBodyY[0] = (int) (y + sinDir * shipLength / 2);

        // Right upper spike
        shipBodyX[1] = (int) (x + cosDir * shipLength / 6 + perpCosDir * shipWidth / 2);
        shipBodyY[1] = (int) (y + sinDir * shipLength / 6 + perpSinDir * shipWidth / 2);

        // Right lower wing
        shipBodyX[2] = (int) (x - cosDir * shipLength / 3 + perpCosDir * shipWidth / 3);
        shipBodyY[2] = (int) (y - sinDir * shipLength / 3 + perpSinDir * shipWidth / 3);

        // Back center
        shipBodyX[3] = (int) (x - cosDir * shipLength / 2);
        shipBodyY[3] = (int) (y - sinDir * shipLength / 2);

        // Left lower wing
        shipBodyX[4] = (int) (x - cosDir * shipLength / 3 - perpCosDir * shipWidth / 3);
        shipBodyY[4] = (int) (y - sinDir * shipLength / 3 - perpSinDir * shipWidth / 3);

        // Left upper spike
        shipBodyX[5] = (int) (x + cosDir * shipLength / 6 - perpCosDir * shipWidth / 2);
        shipBodyY[5] = (int) (y + sinDir * shipLength / 6 - perpSinDir * shipWidth / 2);

        drawShipWithShadow(g, shipBodyX, shipBodyY, color);

        // Add extra attack spikes
        g.setColor(color.darker());
        for (int i = 0; i < 3; i++) {
            int spikeX = (int) (x + cosDir * (shipLength / 4 + i * 2));
            int spikeY = (int) (y + sinDir * (shipLength / 4 + i * 2));
            g.fillOval(spikeX - 1, spikeY - 1, 2, 2);
        }
    }

    /**
     * Draws a bulky defensive ship with shield-like appearance
     */
    private void drawDefenceShip(Graphics2D g, int x, int y, double cosDir, double sinDir, double perpCosDir,
            double perpSinDir, Color color) {
        int shipLength = 14;
        int shipWidth = 12;

        // Ship body points (bulky shield shape)
        int[] shipBodyX = new int[8];
        int[] shipBodyY = new int[8];

        // Front center
        shipBodyX[0] = (int) (x + cosDir * shipLength / 2);
        shipBodyY[0] = (int) (y + sinDir * shipLength / 2);

        // Right front corner
        shipBodyX[1] = (int) (x + cosDir * shipLength / 4 + perpCosDir * shipWidth / 3);
        shipBodyY[1] = (int) (y + sinDir * shipLength / 4 + perpSinDir * shipWidth / 3);

        // Right middle bulge
        shipBodyX[2] = (int) (x + perpCosDir * shipWidth / 2);
        shipBodyY[2] = (int) (y + perpSinDir * shipWidth / 2);

        // Right back corner
        shipBodyX[3] = (int) (x - cosDir * shipLength / 3 + perpCosDir * shipWidth / 3);
        shipBodyY[3] = (int) (y - sinDir * shipLength / 3 + perpSinDir * shipWidth / 3);

        // Back center
        shipBodyX[4] = (int) (x - cosDir * shipLength / 2);
        shipBodyY[4] = (int) (y - sinDir * shipLength / 2);

        // Left back corner
        shipBodyX[5] = (int) (x - cosDir * shipLength / 3 - perpCosDir * shipWidth / 3);
        shipBodyY[5] = (int) (y - sinDir * shipLength / 3 - perpSinDir * shipWidth / 3);

        // Left middle bulge
        shipBodyX[6] = (int) (x - perpCosDir * shipWidth / 2);
        shipBodyY[6] = (int) (y - perpSinDir * shipWidth / 2);

        // Left front corner
        shipBodyX[7] = (int) (x + cosDir * shipLength / 4 - perpCosDir * shipWidth / 3);
        shipBodyY[7] = (int) (y + sinDir * shipLength / 4 - perpSinDir * shipWidth / 3);

        drawShipWithShadow(g, shipBodyX, shipBodyY, color);

        // Add armor plating lines
        g.setColor(color.darker());
        for (int i = -1; i <= 1; i++) {
            int lineStartX = (int) (x - cosDir * shipLength / 4 + perpCosDir * shipWidth / 4 * i);
            int lineStartY = (int) (y - sinDir * shipLength / 4 + perpSinDir * shipWidth / 4 * i);
            int lineEndX = (int) (x + cosDir * shipLength / 6 + perpCosDir * shipWidth / 4 * i);
            int lineEndY = (int) (y + sinDir * shipLength / 6 + perpSinDir * shipWidth / 4 * i);
            g.drawLine(lineStartX, lineStartY, lineEndX, lineEndY);
        }
    }

    /**
     * Draws a sleek, streamlined speed ship
     */
    private void drawSpeedShip(Graphics2D g, int x, int y, double cosDir, double sinDir, double perpCosDir,
            double perpSinDir, Color color) {
        int shipLength = 20;
        int shipWidth = 5;

        // Ship body points (sleek arrow shape)
        int[] shipBodyX = new int[5];
        int[] shipBodyY = new int[5];

        // Front tip (extended for speed)
        shipBodyX[0] = (int) (x + cosDir * shipLength / 2);
        shipBodyY[0] = (int) (y + sinDir * shipLength / 2);

        // Right wing (narrow)
        shipBodyX[1] = (int) (x + cosDir * shipLength / 6 + perpCosDir * shipWidth / 2);
        shipBodyY[1] = (int) (y + sinDir * shipLength / 6 + perpSinDir * shipWidth / 2);

        // Right back
        shipBodyX[2] = (int) (x - cosDir * shipLength / 2 + perpCosDir * shipWidth / 3);
        shipBodyY[2] = (int) (y - sinDir * shipLength / 2 + perpSinDir * shipWidth / 3);

        // Left back
        shipBodyX[3] = (int) (x - cosDir * shipLength / 2 - perpCosDir * shipWidth / 3);
        shipBodyY[3] = (int) (y - sinDir * shipLength / 2 - perpSinDir * shipWidth / 3);

        // Left wing (narrow)
        shipBodyX[4] = (int) (x + cosDir * shipLength / 6 - perpCosDir * shipWidth / 2);
        shipBodyY[4] = (int) (y + sinDir * shipLength / 6 - perpSinDir * shipWidth / 2);

        drawShipWithShadow(g, shipBodyX, shipBodyY, color);

        // Add speed lines/trails
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        for (int i = 1; i <= 3; i++) {
            int trailStartX = (int) (x - cosDir * (shipLength / 2 + i * 3));
            int trailStartY = (int) (y - sinDir * (shipLength / 2 + i * 3));
            int trailEndX = (int) (x - cosDir * shipLength / 3);
            int trailEndY = (int) (y - sinDir * shipLength / 3);
            g.drawLine(trailStartX, trailStartY, trailEndX, trailEndY);
        }
    }

    /**
     * Helper method to draw ship with shadow and highlight effects
     */
    private void drawShipWithShadow(Graphics2D g, int[] shipBodyX, int[] shipBodyY, Color color) {
        // Draw ship shadow/outline first
        g.setColor(Color.BLACK);
        Polygon shadowPoly = new Polygon(shipBodyX, shipBodyY, shipBodyX.length);
        for (int i = 0; i < shipBodyX.length; i++) {
            shadowPoly.xpoints[i] += 1;
            shadowPoly.ypoints[i] += 1;
        }
        g.fillPolygon(shadowPoly);

        // Draw main ship body
        g.setColor(color);
        Polygon shipPoly = new Polygon(shipBodyX, shipBodyY, shipBodyX.length);
        g.fillPolygon(shipPoly);

        // Draw ship highlight
        g.setColor(color.brighter());
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.7f));

        // Highlight on the front part (first 3 points typically form the front)
        int highlightPoints = Math.min(3, shipBodyX.length);
        int[] highlightX = new int[highlightPoints];
        int[] highlightY = new int[highlightPoints];
        for (int i = 0; i < highlightPoints; i++) {
            highlightX[i] = shipBodyX[i];
            highlightY[i] = shipBodyY[i];
        }
        g.fillPolygon(new Polygon(highlightX, highlightY, highlightPoints));

        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));

        // Draw ship outline
        g.setColor(color.darker());
        g.drawPolygon(shipPoly);
    }

    private void drawUnstoppableShipsEffect(Graphics2D g, int x, int y, double direction) {
        // Golden energy aura around the ship
        long currentTime = System.currentTimeMillis();

        // Pulsing golden aura
        float alpha = 0.5f + 0.3f * (float) Math.sin(currentTime * 0.01);
        g.setColor(new Color(255, 215, 0, (int) (alpha * 255))); // Gold color

        // Draw expanding energy rings
        for (int ring = 0; ring < 3; ring++) {
            int ringSize = 20 + ring * 5 + (int) (3 * Math.sin(currentTime * 0.008 + ring));
            g.drawOval(x - ringSize / 2, y - ringSize / 2, ringSize, ringSize);
        }

        // Draw energy sparks around the ship
        for (int i = 0; i < 8; i++) {
            double sparkAngle = (currentTime * 0.005) + (i * Math.PI / 4);
            int sparkDistance = 15 + (int) (3 * Math.sin(currentTime * 0.01 + i));

            int sparkX = x + (int) (sparkDistance * Math.cos(sparkAngle));
            int sparkY = y + (int) (sparkDistance * Math.sin(sparkAngle));

            g.setColor(new Color(255, 255, 150, (int) (alpha * 200)));
            g.fillOval(sparkX - 1, sparkY - 1, 2, 2);
        }
    }

    /**
     * Draws animated thrusters behind the ship
     */
    private void drawThrusters(Graphics2D g, int x, int y, double direction, double speed) {
        // Calculate thruster positions
        double cosDir = Math.cos(direction);
        double sinDir = Math.sin(direction);
        double perpCosDir = Math.cos(direction + Math.PI / 2);
        double perpSinDir = Math.sin(direction + Math.PI / 2);

        int thrusterLength = (int) (8 + (speed / 3)); // Thruster size based on speed
        int thrusterWidth = 4;

        // Main thruster (center)
        int thrusterBackX = (int) (x - cosDir * 8);
        int thrusterBackY = (int) (y - sinDir * 8);

        int thrusterEndX = (int) (thrusterBackX - cosDir * thrusterLength);
        int thrusterEndY = (int) (thrusterBackY - sinDir * thrusterLength);

        // Animate thruster flame
        long currentTime = System.currentTimeMillis();
        double flameIntensity = 0.7 + 0.3 * Math.sin(currentTime / 50.0);
        int animatedLength = (int) (thrusterLength * flameIntensity);

        thrusterEndX = (int) (thrusterBackX - cosDir * animatedLength);
        thrusterEndY = (int) (thrusterBackY - sinDir * animatedLength);

        // Draw thruster flame with gradient effect
        drawThrusterFlame(g, thrusterBackX, thrusterBackY, thrusterEndX, thrusterEndY, perpCosDir, perpSinDir,
                thrusterWidth);

        // Draw smaller side thrusters
        int sideOffset = 6;

        // Right side thruster
        int rightThrusterX = (int) (thrusterBackX + perpCosDir * sideOffset);
        int rightThrusterY = (int) (thrusterBackY + perpSinDir * sideOffset);
        int rightThrusterEndX = (int) (rightThrusterX - cosDir * animatedLength * 0.6);
        int rightThrusterEndY = (int) (rightThrusterY - sinDir * animatedLength * 0.6);
        drawThrusterFlame(g, rightThrusterX, rightThrusterY, rightThrusterEndX, rightThrusterEndY, perpCosDir,
                perpSinDir, thrusterWidth / 2);

        // Left side thruster
        int leftThrusterX = (int) (thrusterBackX - perpCosDir * sideOffset);
        int leftThrusterY = (int) (thrusterBackY - perpSinDir * sideOffset);
        int leftThrusterEndX = (int) (leftThrusterX - cosDir * animatedLength * 0.6);
        int leftThrusterEndY = (int) (leftThrusterY - sinDir * animatedLength * 0.6);
        drawThrusterFlame(g, leftThrusterX, leftThrusterY, leftThrusterEndX, leftThrusterEndY, perpCosDir, perpSinDir,
                thrusterWidth / 2);
    }

    /**
     * Draws an individual thruster flame
     */
    private void drawThrusterFlame(Graphics2D g, int startX, int startY, int endX, int endY,
            double perpCosDir, double perpSinDir, int width) {
        // Create flame shape
        int[] flameX = new int[4];
        int[] flameY = new int[4];

        // Start points (thruster nozzle)
        flameX[0] = (int) (startX + perpCosDir * width / 2);
        flameY[0] = (int) (startY + perpSinDir * width / 2);
        flameX[1] = (int) (startX - perpCosDir * width / 2);
        flameY[1] = (int) (startY - perpSinDir * width / 2);

        // End point (flame tip) - single point for pointed flame
        flameX[2] = endX;
        flameY[2] = endY;
        flameX[3] = endX;
        flameY[3] = endY;

        // Draw flame layers for glow effect

        // Outer flame (orange/red)
        g.setColor(new Color(255, 100, 0, 180));
        g.fillPolygon(new Polygon(flameX, flameY, 4));

        // Inner flame (yellow/white)
        g.setColor(new Color(255, 200, 0, 200));
        // Make inner flame slightly smaller
        for (int i = 0; i < 2; i++) {
            flameX[i] = (int) (flameX[i] * 0.7 + endX * 0.3);
            flameY[i] = (int) (flameY[i] * 0.7 + endY * 0.3);
        }
        g.fillPolygon(new Polygon(flameX, flameY, 4));

        // Core flame (bright white)
        g.setColor(new Color(255, 255, 255, 150));
        for (int i = 0; i < 2; i++) {
            flameX[i] = (int) (flameX[i] * 0.5 + endX * 0.5);
            flameY[i] = (int) (flameY[i] * 0.5 + endY * 0.5);
        }
        g.fillPolygon(new Polygon(flameX, flameY, 4));
    }

    /**
     * Draws a small health bar for the ship
     */
    @SuppressWarnings("unused")
    private void drawHealthBar(Graphics2D g, int x, int y, int currentHealth, int maxHealth) {
        int barWidth = 12;
        int barHeight = 2;
        int barX = x - barWidth / 2;
        int barY = y + 12; // Position below ship

        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);

        // Health portion
        double healthPercent = (double) currentHealth / maxHealth;
        Color healthColor = healthPercent > 0.6 ? Color.GREEN : healthPercent > 0.3 ? Color.YELLOW : Color.RED;
        g.setColor(healthColor);
        int healthWidth = (int) (barWidth * healthPercent);
        g.fillRect(barX, barY, healthWidth, barHeight);

        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    /**
     * Renders all active projectiles
     */
    private void renderProjectiles(Graphics2D g) {
        if (projectiles == null)
            return;

        // Check visual settings for projectiles
        if (!VisualSettings.getInstance().isDisplayProjectiles()) {
            return; // Skip rendering projectiles if disabled
        }

        // projectiles is already a snapshot created in GameMenu, so no need for
        // additional snapshotting
        for (Projectile projectile : projectiles) {
            try {
                if (projectile.isActive()) {
                    drawProjectile(g, projectile);
                }
            } catch (Exception e) {
                System.out.println("Error drawing projectile: " + e.getMessage());
                continue;
            }
        }
    }

    /**
     * Draws a single projectile
     */
    private void drawProjectile(Graphics2D g, Projectile projectile) {
        int x = projectile.getX();
        int y = projectile.getY();
        double direction = projectile.getDirection();

        // Determine projectile color based on source ship's operator
        Color projectileColor;
        if (projectile.getOperator() instanceof Player) {
            projectileColor = VisualSettings.getInstance().getPlayerShipColor().brighter();
        } else {
            // Use planet color with .brighter() for projectiles
            Color baseColor = planetArtist.getOperatorColor(projectile.getOperator());
            projectileColor = baseColor.brighter().brighter(); // Extra bright for visibility
        }

        // Draw projectile body
        g.setColor(projectileColor);

        // Calculate projectile dimensions
        int projectileLength = 8;
        int projectileWidth = 3;

        // Calculate projectile orientation
        double cosDir = Math.cos(direction);
        double sinDir = Math.sin(direction);

        // Draw projectile as a small elongated shape
        int frontX = (int) (x + cosDir * projectileLength / 2);
        int frontY = (int) (y + sinDir * projectileLength / 2);
        int backX = (int) (x - cosDir * projectileLength / 2);
        int backY = (int) (y - sinDir * projectileLength / 2);

        // Draw main projectile body
        g.setColor(projectileColor);
        g.drawLine(backX, backY, frontX, frontY);

        // Draw projectile glow effect
        if (VisualSettings.getInstance().isDisplayEffects()) {
            g.setColor(new Color(projectileColor.getRed(), projectileColor.getGreen(), projectileColor.getBlue(), 100));
            g.fillOval(x - projectileWidth, y - projectileWidth, projectileWidth * 2, projectileWidth * 2);
        }

        // Draw bright projectile core
        g.setColor(Color.WHITE);
        g.fillOval(x - 1, y - 1, 2, 2);
    }

    public Ship findShipAt(int x, int y) {
        if (ships == null)
            return null;

        int shipSize = GameConstants.getShipSize() * 3;

        // ships is already a snapshot created in GameMenu, so no need for additional
        // snapshotting
        for (Ship ship : ships) {
            try {
                int shipX = ship.getX();
                int shipY = ship.getY();

                // Check if mouse is within ship bounds
                if (x >= shipX - shipSize && x <= shipX + shipSize && y >= shipY - shipSize && y <= shipY + shipSize) {
                    return ship;
                }
            } catch (Exception e) {
                System.out.println("Error checking ship: " + e.getMessage());
                continue;
            }
        }
        return null;
    }
}