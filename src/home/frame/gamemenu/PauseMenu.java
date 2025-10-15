package home.frame.gamemenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import home.frame.GameFrame;
import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;
import home.game.Game;

public class PauseMenu {
    private Game game;
    private GameFrame frame;
    private BackgroundArtist backgroundArtist;
    private JDialog pauseDialog = null;
    private SettingsDialog settingsDialog;

    public PauseMenu(Game game, GameFrame frame, BackgroundArtist backgroundArtist) {
        this.game = game;
        this.frame = frame;
        this.backgroundArtist = backgroundArtist;
        this.settingsDialog = new SettingsDialog(frame);
    }

    /**
     * Shows the pause menu with space theme aesthetic
     */
    public void show() {
        if (pauseDialog != null) {
            return; // Already showing
        }

        // Create custom pause dialog
        pauseDialog = new JDialog(frame, "Game Paused", true);
        pauseDialog.setSize(400, 300);
        pauseDialog.setLocationRelativeTo(frame);
        pauseDialog.setUndecorated(true);
        pauseDialog.setBackground(new Color(0, 0, 0, 0));

        // Create custom panel with space theme
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Draw background with space theme
                backgroundArtist.renderBackground(g2d);

                // Draw semi-transparent overlay
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 20, 20);

                // Draw border
                g2d.setColor(new Color(100, 150, 255, 200));
                g2d.drawRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 20, 20);

                g2d.dispose();
            }
        };

        // Add animation timer for background
        Timer pauseAnimationTimer = new Timer(1000 / 60, e -> mainPanel.repaint());
        pauseAnimationTimer.start();

        // Stop animation when dialog is closed
        pauseDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                pauseAnimationTimer.stop();
            }
        });

        mainPanel.setOpaque(false);
        mainPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("GAME PAUSED");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(100, 150, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Reset constraints for buttons
        gbc.gridwidth = 1;
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

        // Resume button
        SpaceButton resumeButton = new SpaceButton("RESUME");
        resumeButton.setColors(new Color(50, 150, 50, 200), new Color(70, 180, 70, 220), new Color(30, 120, 30, 240));
        resumeButton.setPreferredSize(new java.awt.Dimension(120, 40));
        resumeButton.addActionListener(e -> resume());
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(resumeButton, gbc);

        // Restart button
        SpaceButton restartButton = new SpaceButton("RESTART");
        restartButton.setColors(new Color(255, 165, 0, 200), new Color(255, 185, 20, 220), new Color(235, 145, 0, 240));
        restartButton.setPreferredSize(new java.awt.Dimension(120, 40));
        restartButton.addActionListener(e -> {
            resume(); // Close pause dialog
            restartGame();
        });
        gbc.gridx = 1;
        mainPanel.add(restartButton, gbc);

        // Settings button
        SpaceButton settingsButton = new SpaceButton("SETTINGS");
        settingsButton.setColors(new Color(70, 130, 180, 200), new Color(90, 150, 200, 220),
                new Color(50, 110, 160, 240));
        settingsButton.setPreferredSize(new java.awt.Dimension(120, 40));
        settingsButton.addActionListener(e -> {
            // Don't resume - keep pause dialog open and show settings as popup
            openSettings();
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(settingsButton, gbc);

        // Main Menu button
        SpaceButton mainMenuButton = new SpaceButton("MAIN MENU");
        mainMenuButton.setColors(new Color(150, 100, 50, 200), new Color(180, 130, 70, 220),
                new Color(120, 80, 30, 240));
        mainMenuButton.setPreferredSize(new java.awt.Dimension(150, 40));
        mainMenuButton.addActionListener(e -> {
            resume(); // Close pause dialog
            returnToMainMenu();
        });
        gbc.gridx = 1;
        mainPanel.add(mainMenuButton, gbc);

        pauseDialog.add(mainPanel);
        pauseDialog.setVisible(true);
    }

    /**
     * Hides the pause menu
     */
    public void hide() {
        if (pauseDialog != null) {
            pauseDialog.dispose();
            pauseDialog = null;
        }
    }

    /**
     * Resumes the game and hides pause menu
     */
    private void resume() {
        hide();
        game.resume();
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

    /**
     * Restarts the current game
     */
    private void restartGame() {
        // Get current difficulty from the game
        home.game.operators.Difficulty currentDifficulty = game.getDifficulty();

        // Stop current game
        game.stop();

        // Start a new game with the same difficulty
        frame.openGameMenu(currentDifficulty);
    }

    /**
     * Opens the settings dialog as a popup
     */
    private void openSettings() {
        // Show settings dialog as popup - game remains paused
        settingsDialog.show();
    }

    /**
     * Checks if the pause menu is currently visible
     */
    public boolean isVisible() {
        return pauseDialog != null && pauseDialog.isVisible();
    }
}