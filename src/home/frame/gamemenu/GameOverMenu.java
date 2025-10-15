package home.frame.gamemenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import home.frame.CoinIcon;
import home.frame.GameFrame;
import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;
import home.game.Game;
import home.game.operators.Difficulty;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.operators.player.PlayerData;
import home.game.planets.Planet;

public class GameOverMenu {
    private Game game;
    private GameFrame frame;
    private BackgroundArtist backgroundArtist;
    private GameMenu gameMenu;

    public GameOverMenu(Game game, GameFrame frame, BackgroundArtist backgroundArtist, GameMenu gameMenu) {
        this.game = game;
        this.frame = frame;
        this.backgroundArtist = backgroundArtist;
        this.gameMenu = gameMenu;
    }

    /**
     * Shows the win/lose popup dialog with game results using space theme aesthetic
     */
    public void show() {
        Operator winner = game.getWinner();
        boolean playerWon = winner instanceof Player;

        long gameTimeMs = game.getGameDuration();
        long seconds = gameTimeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        String timeStr = String.format("%d:%02d", minutes, seconds);
        String title = playerWon ? "VICTORY!" : "DEFEAT!";
        String subtitle = playerWon ? "You have conquered the galaxy!" : "Your forces have been overwhelmed!";

        // Handle rewards and best times
        PlayerData playerData = PlayerData.getInstance();
        Difficulty difficulty = game.getDifficulty();
        final int coinsEarned;
        boolean newBestTime = false;

        // Calculate planet counts for aggressive conqueror bonus (needed for both
        // won/lost cases)
        final int totalPlanets = game.getPlanets().size();
        int playerPlanets = 0;
        for (Planet planet : game.getPlanets()) {
            if (planet.getOperator() == game.getPlayer()) {
                playerPlanets++;
            }
        }
        int uncapturedPlanets = totalPlanets - playerPlanets;

        if (playerWon) {
            // Calculate and award coins with aggressive conqueror bonus
            coinsEarned = playerData.calculateReward(gameTimeMs, difficulty, true, totalPlanets, uncapturedPlanets);
            playerData.addCoins(coinsEarned);

            // Update best time
            Long currentBest = playerData.getBestTime(difficulty);
            if (currentBest == null || gameTimeMs < currentBest) {
                newBestTime = true;
                playerData.updateBestTime(difficulty, gameTimeMs);
            }
        } else {
            coinsEarned = 0;
        }

        // Create custom game over dialog
        JDialog popup = new JDialog(frame, title, true);
        popup.setSize(500, 450); // Increased height for aggressive conqueror bonus
        popup.setLocationRelativeTo(frame);
        popup.setUndecorated(true);
        popup.setBackground(new Color(0, 0, 0, 0));

        // Create custom panel with space theme
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Draw background with space theme
                backgroundArtist.renderBackground(g2d);

                // Draw semi-transparent overlay
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 20, 20);

                // Draw border with win/lose color
                Color borderColor = playerWon ? new Color(50, 255, 50, 200) : new Color(255, 50, 50, 200);
                g2d.setColor(borderColor);
                g2d.drawRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 20, 20);

                // Draw inner glow effect
                g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 50));
                for (int i = 1; i <= 5; i++) {
                    g2d.drawRoundRect(20 - i, 20 - i, getWidth() - 40 + 2 * i, getHeight() - 40 + 2 * i, 20, 20);
                }

                g2d.dispose();
            }
        };

        // Add animation timer for background
        javax.swing.Timer animationTimer = new javax.swing.Timer(1000 / 60, e -> mainPanel.repaint());
        animationTimer.start();

        // Stop animation when dialog is closed
        popup.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                animationTimer.stop();
            }
        });

        mainPanel.setOpaque(false);
        mainPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(15, 15, 15, 15);

        // Main title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 42));
        titleLabel.setForeground(playerWon ? new Color(50, 255, 50) : new Color(255, 50, 50));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        mainPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(5, 15, 10, 15);
        mainPanel.add(subtitleLabel, gbc);

        // Game time
        JLabel timeLabel = new JLabel("Battle Duration: " + timeStr);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timeLabel.setForeground(new Color(100, 150, 255));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(10, 15, 10, 15);
        mainPanel.add(timeLabel, gbc);

        // Best time indicator
        if (newBestTime && playerWon) {
            JLabel bestTimeLabel = new JLabel("NEW BEST TIME!");
            bestTimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
            bestTimeLabel.setForeground(new Color(255, 215, 0)); // Gold color
            bestTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 3;
            gbc.insets = new java.awt.Insets(5, 15, 10, 15);
            mainPanel.add(bestTimeLabel, gbc);
        }

        // Coin reward with icon
        if (playerWon && coinsEarned > 0) {
            JLabel coinLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Font font = new Font("Arial", Font.BOLD, 18);
                    String text = "+" + coinsEarned + " Earned!";

                    // Calculate position to center the coin + text
                    int totalWidth = CoinIcon.getCoinWithTextWidth(g2d, text, font);
                    int startX = (getWidth() - totalWidth) / 2 + font.getSize() / 2;
                    int y = getHeight() / 2;

                    CoinIcon.drawCoinWithText(g2d, startX, y, text, font, new Color(255, 215, 0));
                    g2d.dispose();
                }
            };
            coinLabel.setPreferredSize(new Dimension(300, 25));
            coinLabel.setOpaque(false);
            gbc.gridy = newBestTime ? 4 : 3;
            gbc.insets = new java.awt.Insets(5, 15, 10, 15);
            mainPanel.add(coinLabel, gbc);

            // Show aggressive conqueror bonus if earned
            if (uncapturedPlanets > 0) {
                final int baseReward = playerData.calculateReward(gameTimeMs, difficulty, true, 0, 0);
                final int bonusAmount = coinsEarned - baseReward;

                JLabel bonusLabel = new JLabel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        Font font = new Font("Arial", Font.BOLD, 14);
                        String text = "Aggressive Conqueror Bonus: +" + bonusAmount;

                        // Calculate position to center the coin + text
                        int totalWidth = CoinIcon.getCoinWithTextWidth(g2d, text, font);
                        int startX = (getWidth() - totalWidth) / 2 + font.getSize() / 2;
                        int y = getHeight() / 2;

                        CoinIcon.drawCoinWithText(g2d, startX, y, text, font, new Color(255, 150, 0));
                        g2d.dispose();
                    }
                };
                bonusLabel.setPreferredSize(new Dimension(350, 20));
                bonusLabel.setOpaque(false);
                gbc.gridy = newBestTime ? 5 : 4;
                gbc.insets = new java.awt.Insets(0, 15, 25, 15);
                mainPanel.add(bonusLabel, gbc);
            } else {
                gbc.insets = new java.awt.Insets(5, 15, 25, 15);
            }
        }

        // Reset constraints for buttons
        gbc.gridwidth = 1;
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

        // Calculate button row (after all the labels)
        int buttonRow = 3;
        if (newBestTime && playerWon)
            buttonRow++;
        if (playerWon && coinsEarned > 0)
            buttonRow++;
        if (playerWon && uncapturedPlanets > 0)
            buttonRow++; // Add extra row for aggressive conqueror bonus

        // Play Again button
        SpaceButton playAgainButton = new SpaceButton("PLAY AGAIN");
        playAgainButton.setColors(new Color(50, 150, 50, 200), new Color(70, 180, 70, 220),
                new Color(30, 120, 30, 240));
        playAgainButton.setPreferredSize(new java.awt.Dimension(140, 45));
        playAgainButton.addActionListener(e -> {
            popup.dispose();
            restartGame();
        });
        gbc.gridx = 0;
        gbc.gridy = buttonRow;
        mainPanel.add(playAgainButton, gbc);

        // Main Menu button
        SpaceButton mainMenuButton = new SpaceButton("MAIN MENU");
        mainMenuButton.setColors(new Color(150, 100, 50, 200), new Color(180, 130, 70, 220),
                new Color(120, 80, 30, 240));
        mainMenuButton.setPreferredSize(new java.awt.Dimension(140, 45));
        mainMenuButton.addActionListener(e -> {
            popup.dispose();
            returnToMainMenu();
        });
        gbc.gridx = 1;
        gbc.gridy = buttonRow;
        mainPanel.add(mainMenuButton, gbc);

        popup.add(mainPanel);
        popup.setVisible(true);
    }

    /**
     * Restarts the game with a new instance
     */
    private void restartGame() {
        // Reset the current game (this handles stopping and restarting)
        game.resetGame();

        // Reset the win popup flag in GameMenu
        gameMenu.resetWinPopupFlag();

        // Start the game again
        game.start();
    }

    /**
     * Returns to the main menu
     */
    private void returnToMainMenu() {
        // Stop current game
        game.stop();

        // Use GameFrame method to open main menu
        frame.openMainMenu();
    }
}