package home.frame.gamemenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.swing.JPanel;

import home.frame.CoinIcon;
import home.frame.GameFrame;
import home.frame.background.BackgroundArtist;
import home.game.Explosion;
import home.game.Game;
import home.game.GameConstants;
import home.game.Projectile;
import home.game.Ship;
import home.game.VisualSettings;
import home.game.abilities.AbilityType;
import home.sounds.Sound;
import home.game.challenges.AchievementNotification;
import home.game.challenges.ChallengeManager;
import home.game.challenges.ProgressNotification;
import home.game.operators.player.Player;
import home.game.operators.player.PlayerData;
import home.game.planets.Planet;

public class GameMenu extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private Game game;
    private Thread gameThread;
    private int frameCount = 0;
    private long lastTime = System.currentTimeMillis();

    private PlanetArtist planetArtist;
    private ShipArtist shipArtist;
    private BackgroundArtist backgroundArtist;
    private EffectsArtist effectsArtist;
    private OperatorIndicatorRenderer operatorIndicatorRenderer;

    // Menu instances
    private PauseMenu pauseMenu;
    private GameOverMenu gameOverMenu;

    // Mouse interaction variables
    private Planet selectedPlanet = null;
    private Planet hoveredPlanet = null;
    private Point mousePosition = null;
    private boolean isDragging = false;

    // Ability interaction variables
    private AbilityType hoveredAbility = null;
    private boolean showAbilityTooltip = false;
    private String abilityTooltipText = "";
    private Point tooltipPosition = null;

    // Win condition tracking
    private boolean winPopupShown = false;

    // Sound notification tracking
    private int lastNotificationCount = 0;

    // Click targeting
    private Planet clickedPlanet = null;
    private long clickedPlanetClearTime = 0; // Time to clear clickedPlanet to show arrows briefly after targeting

    public GameMenu(Game game, GameFrame frame) {
        this.game = game;
        backgroundArtist = new BackgroundArtist();
        planetArtist = new PlanetArtist();
        shipArtist = new ShipArtist(planetArtist);
        shipArtist.setGame(game);
        effectsArtist = new EffectsArtist(game);
        operatorIndicatorRenderer = new OperatorIndicatorRenderer(game, GameConstants.getGameWidth(), planetArtist,
                effectsArtist);

        // Initialize menu instances
        pauseMenu = new PauseMenu(game, frame, backgroundArtist);
        gameOverMenu = new GameOverMenu(game, frame, backgroundArtist, this);

        gameThread = new Thread(() -> {
            while (true) {
                // Check for win condition
                if (game.isGameEnded() && !winPopupShown) {
                    winPopupShown = true;
                    gameOverMenu.show();
                }

                repaint();
                frameCount++;
                if (System.currentTimeMillis() - lastTime >= 1000) {
                    frame.setTitle("Space Game - FPS: " + frameCount);
                    frameCount = 0;
                    lastTime = System.currentTimeMillis();
                }
                try {
                    Thread.sleep(1000 / GameConstants.getTargetTPS()); // target FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.start();
        game.start();

        // Add mouse and key listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        // Disable focus traversal for TAB key so it reaches our KeyListener
        setFocusTraversalKeysEnabled(false);
    }

    @Override
    protected void paintComponent(Graphics g2) {
        super.paintComponent(g2);
        Graphics2D g = (Graphics2D) g2;
        backgroundArtist.renderBackground(g);
        renderGame(g);
    }

    private void renderGame(Graphics2D g) {
        // Check if it's time to clear the clicked planet after targeting
        if (clickedPlanetClearTime > 0 && System.currentTimeMillis() >= clickedPlanetClearTime) {
            clickedPlanet = null;
            clickedPlanetClearTime = 0;
        }

        // Create safe snapshots of game collections to avoid concurrent modification
        List<Planet> planetsSnapshot;
        List<Ship> shipsSnapshot;
        List<Projectile> projectilesSnapshot;

        try {
            planetsSnapshot = new ArrayList<>(game.getPlanets());
            shipsSnapshot = new ArrayList<>(game.getShips());
            projectilesSnapshot = new ArrayList<>(game.getProjectiles());
        } catch (Exception e) {
            // If we can't create snapshots, skip rendering this frame
            return;
        }

        // Render planets
        planetArtist.setPlanets(planetsSnapshot);
        planetArtist.setHoveredPlanet(hoveredPlanet);
        planetArtist.setClickedPlanet(clickedPlanet);
        planetArtist.setSelectedPlanet(selectedPlanet);
        planetArtist.renderPlanets(g);

        // Render ability effects over planets
        effectsArtist.renderAbilityEffects(g);

        // Render ships and projectiles
        shipArtist.setShips(shipsSnapshot);
        shipArtist.setProjectiles(projectilesSnapshot);
        shipArtist.renderShips(g);

        // Render explosions
        List<Explosion> explosionsSnapshot;
        try {
            explosionsSnapshot = new ArrayList<>(game.getExplosions());
            for (Explosion explosion : explosionsSnapshot) {
                explosion.render(g);
            }
        } catch (Exception e) {
            // If we can't create explosion snapshot, skip explosion rendering
        }

        // Draw connection line when dragging
        if (isDragging && selectedPlanet != null && mousePosition != null) {
            g.setColor(Color.WHITE);
            g.drawLine(selectedPlanet.getX(), selectedPlanet.getY(), mousePosition.x, mousePosition.y);
        }

        // Draw connection line when click targeting and hovering over another planet
        if (clickedPlanet != null && hoveredPlanet != null && hoveredPlanet != clickedPlanet
                && clickedPlanet.getOperator() instanceof Player) {
            g.setColor(Color.WHITE);
            g.drawLine(clickedPlanet.getX(), clickedPlanet.getY(), hoveredPlanet.getX(), hoveredPlanet.getY());
        }

        // Render game timer and coin preview
        renderGameTimer(g);

        // Render ability diamonds
        renderAbilityDiamonds(g);

        // Render operator indicators
        operatorIndicatorRenderer.renderOperatorIndicators(g);

        // Render achievement notifications
        renderAchievementNotifications(g);
    }

    private void renderGameTimer(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate elapsed time
        long elapsedTime = game.getElapsedTime();
        int minutes = (int) (elapsedTime / 60000);
        int seconds = (int) ((elapsedTime % 60000) / 1000);
        String timeText = String.format("%d:%02d", minutes, seconds);

        // Calculate potential coin reward with aggressive conqueror bonus
        PlayerData playerData = PlayerData.getInstance();

        // Calculate planet counts for aggressive bonus
        int totalPlanets = game.getPlanets().size();
        int playerPlanets = 0;
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() == game.getPlayer()) {
                playerPlanets++;
            }
        }
        int uncapturedPlanets = totalPlanets - playerPlanets;

        int baseReward = playerData.calculatePotentialReward(elapsedTime, game.getDifficulty());
        int bonusReward = playerData.calculatePotentialRewardWithBonus(elapsedTime, game.getDifficulty(), totalPlanets,
                uncapturedPlanets);

        // Set up fonts and positions
        Font timeFont = new Font("Arial", Font.BOLD, 16);
        Font coinFont = new Font("Arial", Font.BOLD, 14);
        Font bonusFont = new Font("Arial", Font.PLAIN, 12);

        int x = 20;
        int timeY = 30;
        int coinY = 55;
        int bonusY = 75;

        // Draw time
        g.setFont(timeFont);
        g.setColor(Color.WHITE);
        g.drawString("Time: " + timeText, x, timeY);

        // Draw coin preview with icon
        g.setFont(coinFont);
        String coinText = "Base Reward: " + baseReward;
        CoinIcon.drawCoinWithText(g, x, coinY, coinText, coinFont, new Color(255, 215, 0));

        // Draw aggressive conqueror bonus if applicable
        if (uncapturedPlanets > 0) {
            g.setFont(bonusFont);
            String bonusText = "Aggressive Bonus: +" + (bonusReward - baseReward) + " (" + uncapturedPlanets
                    + " uncaptured)";
            CoinIcon.drawCoinWithText(g, x, bonusY, bonusText, bonusFont, new Color(255, 150, 0));
        }
    }

    private void renderAchievementNotifications(Graphics2D g) {
        ChallengeManager challengeManager = ChallengeManager.getInstance();

        int currentY = 20; // Starting Y position for notifications

        // Render all active achievement completion notifications (higher priority)
        Queue<AchievementNotification> achievementQueue = challengeManager.getPendingNotifications();
        List<AchievementNotification> activeAchievements = new ArrayList<>();

        // Collect all non-expired achievement notifications
        for (AchievementNotification notif : achievementQueue) {
            if (!notif.isExpired()) {
                activeAchievements.add(notif);
            }
        }

        // Play sound for new achievement notifications
        if (activeAchievements.size() > lastNotificationCount) {
            game.getSoundManager().play(Sound.ACHIEVEMENT_UNLOCK);
        }
        lastNotificationCount = activeAchievements.size();

        // Render all achievement notifications
        for (AchievementNotification achNotif : activeAchievements) {
            renderCompletionNotification(g, achNotif, currentY);
            currentY += 90; // Space between notifications (80 height + 10 margin)
        }

        // Then render all active progress notifications below achievement notifications
        Queue<ProgressNotification> progressQueue = challengeManager.getPendingProgressNotifications();
        List<ProgressNotification> activeProgress = new ArrayList<>();

        // Collect all non-expired progress notifications
        for (ProgressNotification notif : progressQueue) {
            if (!notif.isExpired()) {
                activeProgress.add(notif);
            }
        }

        // Render all progress notifications
        for (ProgressNotification progNotif : activeProgress) {
            renderProgressNotification(g, progNotif, currentY);
            currentY += 80; // Space between notifications (70 height + 10 margin)
        }
    }

    private void renderCompletionNotification(Graphics2D g, AchievementNotification notification, int y) {
        int width = 350;
        int height = 80;
        int x = getWidth() - width - 20;

        // Background with slight transparency and rarity color border
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, width, height, 15, 15);

        // Border with rarity color
        g.setColor(notification.getRarity().getColor());
        g.setStroke(new java.awt.BasicStroke(3));
        g.drawRoundRect(x, y, width, height, 15, 15);
        g.setStroke(new java.awt.BasicStroke(1));

        // Achievement completed text
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("ACHIEVEMENT UNLOCKED!", x + 15, y + 25);

        // Challenge name
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(notification.getRarity().getColor());
        String name = notification.getChallengeName();
        if (name.length() > 25) {
            name = name.substring(0, 22) + "...";
        }
        g.drawString(name, x + 15, y + 45);

        // Rewards
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.YELLOW);
        String rewardText = "+" + notification.getCoinReward() + " coins, +" + notification.getScoreReward() + " score";
        g.drawString(rewardText, x + 15, y + 65);

        // Rarity indicator in top right
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(notification.getRarity().getColor());
        String rarityText = notification.getRarity().name();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(rarityText, x + width - fm.stringWidth(rarityText) - 15, y + 20);
    }

    private void renderProgressNotification(Graphics2D g, ProgressNotification notification, int y) {
        int width = 300;
        int height = 70;
        int x = getWidth() - width - 20;

        // Background with slight transparency and rarity color border
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, width, height, 12, 12);

        // Border with rarity color but dimmer
        Color borderColor = new Color(notification.getRarity().getColor().getRed(),
                notification.getRarity().getColor().getGreen(), notification.getRarity().getColor().getBlue(), 150);
        g.setColor(borderColor);
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 12, 12);
        g.setStroke(new java.awt.BasicStroke(1));

        // Progress text
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(200, 200, 255));
        g.drawString("PROGRESS MADE", x + 15, y + 20);

        // Challenge name
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        String name = notification.getChallengeName();
        if (name.length() > 28) {
            name = name.substring(0, 25) + "...";
        }
        g.drawString(name, x + 15, y + 38);

        // Progress bar
        int progressBarX = x + 15;
        int progressBarY = y + 45;
        int progressBarWidth = width - 80;
        int progressBarHeight = 8;

        // Progress bar background
        g.setColor(new Color(100, 100, 100, 150));
        g.fillRoundRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight, 4, 4);

        // Progress bar fill
        int fillWidth = (int) (progressBarWidth * notification.getProgressPercentage());
        Color progressColor = notification.getProgressPercentage() > 0.8f ? new Color(50, 200, 50)
                : notification.getProgressPercentage() > 0.5f ? new Color(255, 200, 0) : new Color(100, 150, 255);
        g.setColor(progressColor);
        g.fillRoundRect(progressBarX, progressBarY, fillWidth, progressBarHeight, 4, 4);

        // Progress text
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.setColor(Color.WHITE);
        String progressText = notification.getCurrentProgress() + "/" + notification.getTargetProgress();
        g.drawString(progressText, x + width - 65, y + 53);
    }

    // Helper method to find planet at given coordinates
    private Planet findPlanetAt(int x, int y) {
        for (Planet planet : game.getPlanets()) {
            int planetX = planet.getX();
            int planetY = planet.getY();
            double planetRadius = planet.getActualRadius();

            // Check if mouse is within planet bounds
            double distance = Math.sqrt(Math.pow(x - planetX, 2) + Math.pow(y - planetY, 2));
            if (distance <= planetRadius) {
                return planet;
            }
        }
        return null;
    }

    // MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {
        // Request focus when panel is clicked to ensure key events work
        requestFocusInWindow();

        click(e.getX(), e.getY());
    }

    private void click(int x, int y) {
        // Check if clicked on an ability diamond
        AbilityType clickedAbility = getAbilityAtPoint(x, y);
        if (clickedAbility != null) {
            // Note: Sound is played in AbilityManager.activateAbility()
            game.getAbilityManager().activateAbility(clickedAbility);
            return;
        }
        Planet newClickedPlanet = findPlanetAt(x, y);
        if (clickedPlanet != null && newClickedPlanet == clickedPlanet) {
            // Clicked the same planet again - deselect
            clickedPlanet = null;
        } else if (clickedPlanet == null && newClickedPlanet != null) {
            // First click on a planet - select it
            clickedPlanet = newClickedPlanet;
            game.getSoundManager().play(Sound.PLANET_SELECT);
        } else if (clickedPlanet != null && newClickedPlanet != null && clickedPlanet != newClickedPlanet) {
            // Second click on a different planet - attempt targeting
            if (clickedPlanet.getOperator() instanceof Player) {
                clickedPlanet.attemptTargeting(newClickedPlanet);
                game.getSoundManager().play(Sound.SHIP_DEPLOY);
                // Keep the clicked planet visible for 1 second to show targeting arrows
                clickedPlanetClearTime = System.currentTimeMillis() + 1000;
            } else {
                clickedPlanet = null; // Immediately clear for non-player planets
            }
        } else {
            // Clicked empty space or invalid scenario - deselect any selected planet
            clickedPlanet = null;
        }

    }

    private long pressedAt = 0;

    @Override
    public void mousePressed(MouseEvent e) {
        selectedPlanet = findPlanetAt(e.getX(), e.getY());
        pressedAt = System.currentTimeMillis();
        // Only allow drag connection creation from Player-owned planets if not in click
        // targeting mode
        if (selectedPlanet != null && selectedPlanet.getOperator() instanceof Player && clickedPlanet == null) {
            isDragging = true;
            mousePosition = new Point(e.getX(), e.getY());
            // Enable slow mode for precise targeting
            game.getEngine().enableSlowMode();
        } else {
            selectedPlanet = null;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragging && selectedPlanet != null && selectedPlanet.getOperator() instanceof Player) {
            Planet targetPlanet = findPlanetAt(e.getX(), e.getY());
            if (targetPlanet != null && targetPlanet != selectedPlanet) {
                selectedPlanet.attemptTargeting(targetPlanet);
            }
        }

        // Disable slow mode when drag operation ends
        if (isDragging) {
            game.getEngine().disableSlowMode();
        }

        if (System.currentTimeMillis() - pressedAt < 300) {
            click(e.getX(), e.getY());
        }
        selectedPlanet = null;
        isDragging = false;
        mousePosition = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not needed for our implementation
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hoveredPlanet = null;
    }

    // MouseMotionListener methods
    @Override
    public void mouseDragged(MouseEvent e) {
        Planet newHoveredPlanet = findPlanetAt(e.getX(), e.getY());
        if (newHoveredPlanet != hoveredPlanet) {
            hoveredPlanet = newHoveredPlanet;
        }
        if (isDragging && selectedPlanet != null) {
            mousePosition = new Point(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Planet newHoveredPlanet = findPlanetAt(e.getX(), e.getY());
        if (newHoveredPlanet != hoveredPlanet) {
            hoveredPlanet = newHoveredPlanet;
        }

        // Check for ability tooltip
        AbilityType newHoveredAbility = getAbilityAtPoint(e.getX(), e.getY());
        if (newHoveredAbility != hoveredAbility) {
            hoveredAbility = newHoveredAbility;
            if (hoveredAbility != null) {
                PlayerData playerData = PlayerData.getInstance();
                showAbilityTooltip = true;
                abilityTooltipText = hoveredAbility.getTooltipText(playerData.getAbilityLevel(hoveredAbility));
                tooltipPosition = new Point(e.getX() + 15, e.getY() - 10);
            } else {
                showAbilityTooltip = false;
                abilityTooltipText = "";
                tooltipPosition = null;
            }
        }

        // Update tooltip position if hovering over ability
        if (showAbilityTooltip && hoveredAbility != null) {
            tooltipPosition = new Point(e.getX() + 15, e.getY() - 10);
        }
    }

    // KeyListener methods
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            togglePause();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            // Cancel any ongoing targeting operations
            if (isDragging) {
                game.getEngine().disableSlowMode();
                isDragging = false;
                selectedPlanet = null;
                mousePosition = null;
            }
            if (clickedPlanet != null) {
                clickedPlanet = null;
                clickedPlanetClearTime = 0;
            }
            togglePause();
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
            // Toggle targeting line visibility
            VisualSettings settings = VisualSettings.getInstance();
            settings.setDisplayConnectionLines(!settings.isDisplayConnectionLines());
        } else {
            // Check for ability keybinds
            VisualSettings settings = VisualSettings.getInstance();
            AbilityType ability = settings.getAbilityForKey(e.getKeyCode());
            if (ability != null) {
                activateAbilityFromKeybind(ability);
            }
        }
    }

    private void activateAbilityFromKeybind(AbilityType ability) {
        // Only activate if game is not paused
        if (game.getEngine().isPaused()) {
            return;
        }

        PlayerData playerData = PlayerData.getInstance();

        // Check if player has unlocked this ability
        if (!playerData.isAbilityUnlocked(ability)) {
            return;
        }

        // Try to activate the ability
        if (game.getAbilityManager().activateAbility(ability)) {
            // Ability activated successfully - could add visual feedback here
            System.out.println("Activated ability via keybind: " + ability.getDisplayName());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not needed
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed
    }

    private void renderAbilityDiamonds(Graphics2D g) {
        PlayerData playerData = PlayerData.getInstance();
        int diamondSize = 50;
        int spacing = 10;
        int startX = 20;
        int startY = GameConstants.getGameHeight() - 80;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int index = 0;
        for (AbilityType abilityType : AbilityType.values()) {
            if (!playerData.isAbilityUnlocked(abilityType)) {
                continue; // Skip locked abilities
            }

            int x = startX;
            int y = startY - (index * (diamondSize + spacing));

            // Check if ability is on cooldown
            long remainingCooldown = game.getAbilityManager().getRemainingCooldown(abilityType);
            boolean onCooldown = remainingCooldown > 0;
            boolean abilityActive = game.getAbilityManager().isAbilityActive(abilityType);

            // Draw diamond background
            Color diamondColor;
            if (abilityActive) {
                diamondColor = new Color(0, 255, 0, 180); // Green when active
            } else if (onCooldown) {
                diamondColor = new Color(128, 128, 128, 180); // Gray when on cooldown
            } else {
                diamondColor = new Color(0, 150, 255, 180); // Blue when ready
            }

            g.setColor(diamondColor);
            drawDiamond(g, x + diamondSize / 2, y + diamondSize / 2, diamondSize);

            // Draw ability icon
            Graphics2D g2d = (Graphics2D) g.create();
            abilityType.drawIcon(g2d, x + diamondSize / 2, y + diamondSize / 2, diamondSize - 8, true);
            g2d.dispose();

            // Draw cooldown overlay if on cooldown
            if (onCooldown) {
                double cooldownPercent = remainingCooldown / (double) GameConstants.getBaseAbilityCooldown(); // base
                                                                                                              // cooldown
                g.setColor(new Color(0, 0, 0, 150));
                int overlayHeight = (int) (diamondSize * cooldownPercent);
                g.fillRect(x, y + diamondSize - overlayHeight, diamondSize, overlayHeight);

                // Draw cooldown time text
                g.setColor(Color.WHITE);
                Font timeFont = new Font("Arial", Font.PLAIN, 10);
                g.setFont(timeFont);
                String timeText = String.format("%.1f", remainingCooldown / 1000.0);
                FontMetrics timeFm = g.getFontMetrics();
                int timeX = x + (diamondSize - timeFm.stringWidth(timeText)) / 2;
                int timeY = y + diamondSize + 12;
                g.drawString(timeText, timeX, timeY);
            }

            index++;
        }

        // Draw tooltip if hovering
        if (showAbilityTooltip && tooltipPosition != null && !abilityTooltipText.isEmpty()) {
            drawAbilityTooltip(g, tooltipPosition.x, tooltipPosition.y, abilityTooltipText);
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawDiamond(Graphics2D g, int centerX, int centerY, int size) {
        int halfSize = size / 2;
        int[] xPoints = { centerX, // top
                centerX + halfSize, // right
                centerX, // bottom
                centerX - halfSize // left
        };
        int[] yPoints = { centerY - halfSize, // top
                centerY, // right
                centerY + halfSize, // bottom
                centerY // left
        };

        g.fillPolygon(xPoints, yPoints, 4);
        g.setColor(Color.WHITE);
        g.drawPolygon(xPoints, yPoints, 4);
    }

    private void drawAbilityTooltip(Graphics2D g, int x, int y, String text) {
        Font tooltipFont = new Font("Arial", Font.PLAIN, 12);
        g.setFont(tooltipFont);
        FontMetrics fm = g.getFontMetrics();

        String[] lines = text.split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }

        int padding = 8;
        int tooltipWidth = maxWidth + padding * 2;
        int tooltipHeight = lines.length * fm.getHeight() + padding * 2;

        // Adjust tooltip position to stay on screen
        if (x + tooltipWidth > GameConstants.getGameWidth()) {
            x = GameConstants.getGameWidth() - tooltipWidth;
        }
        if (y + tooltipHeight > GameConstants.getGameHeight()) {
            y = GameConstants.getGameHeight() - tooltipHeight;
        }

        // Draw tooltip background
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(x, y, tooltipWidth, tooltipHeight);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, tooltipWidth, tooltipHeight);

        // Draw tooltip text
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], x + padding, y + padding + fm.getAscent() + i * fm.getHeight());
        }
    }

    private AbilityType getAbilityAtPoint(int x, int y) {
        PlayerData playerData = PlayerData.getInstance();
        int diamondSize = 50;
        int spacing = 10;
        int startX = 20;
        int startY = GameConstants.getGameHeight() - 80;

        int index = 0;
        for (AbilityType abilityType : AbilityType.values()) {
            if (!playerData.isAbilityUnlocked(abilityType)) {
                continue;
            }

            int abilityX = startX;
            int abilityY = startY - (index * (diamondSize + spacing));

            if (x >= abilityX && x <= abilityX + diamondSize && y >= abilityY && y <= abilityY + diamondSize) {
                return abilityType;
            }

            index++;
        }

        return null;
    }

    private void togglePause() {
        if (game.isPaused()) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    /**
     * Pauses the game and shows pause menu
     */
    private void pauseGame() {
        if (game.isGameEnded()) {
            return; // Don't allow pause if game is already ended
        }

        game.pause();
        pauseMenu.show();
    }

    /**
     * Resumes the game and hides pause menu
     */
    private void resumeGame() {
        pauseMenu.hide();
        game.resume();
        requestFocusInWindow(); // Restore focus for key events
    }

    /**
     * Resets the win popup shown flag for new games
     */
    public void resetWinPopupFlag() {
        winPopupShown = false;
    }

}
