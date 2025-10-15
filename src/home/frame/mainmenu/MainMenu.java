package home.frame.mainmenu;

import javax.swing.*;
import java.awt.*;

import home.game.GameConstants;
import home.game.operators.Difficulty;
import home.frame.GameFrame;
import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;

public class MainMenu extends JPanel {

    private BackgroundArtist backgroundArtist;
    private GameFrame frame;

    // Animation variables
    private Timer animationTimer;

    public MainMenu(GameFrame frame) {
        this.frame = frame;
        this.backgroundArtist = new BackgroundArtist();

        setupLayout();
        setupUI();
        startAnimation();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(GameConstants.getGameWidth(), GameConstants.getGameHeight()));
    }

    private void setupUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("PLANET CONQUEST");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(100, 150, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Select Difficulty");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 30, 10);
        add(subtitleLabel, gbc);

        // Reset constraints for buttons
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Difficulty buttons
        createDifficultyButton("EASY", Difficulty.EASY, new Color(50, 150, 50, 200),
                new Color(70, 180, 70, 220), new Color(30, 120, 30, 240), gbc, 0, 2);

        createDifficultyButton("MEDIUM", Difficulty.MEDIUM, new Color(150, 150, 50, 200),
                new Color(180, 180, 70, 220), new Color(120, 120, 30, 240), gbc, 1, 2);

        createDifficultyButton("HARD", Difficulty.HARD, new Color(150, 100, 50, 200),
                new Color(180, 130, 70, 220), new Color(120, 80, 30, 240), gbc, 0, 3);

        createDifficultyButton("EXTREME", Difficulty.EXTREME, new Color(150, 50, 50, 200),
                new Color(180, 70, 70, 220), new Color(120, 30, 30, 240), gbc, 1, 3);

        // Shop button
        SpaceButton shopButton = new SpaceButton("SHOP");
        shopButton.setColors(new Color(255, 215, 0, 200), new Color(255, 235, 50, 220), new Color(200, 165, 0, 240));
        shopButton.addActionListener(e -> {
            frame.remove(this);
            frame.openShop();
            frame.revalidate();
            frame.repaint();
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(shopButton, gbc);

        // Achievements button
        SpaceButton achievementsButton = new SpaceButton("ACHIEVEMENTS");
        achievementsButton.setColors(new Color(128, 0, 128, 200), new Color(148, 20, 148, 220),
                new Color(108, 0, 108, 240));
        achievementsButton.addActionListener(e -> {
            frame.remove(this);
            frame.openAchievements();
            frame.revalidate();
            frame.repaint();
        });
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(achievementsButton, gbc);

        // Help button
        SpaceButton helpButton = new SpaceButton("HELP & TUTORIAL");
        helpButton.setColors(new Color(50, 180, 50, 200), new Color(70, 200, 70, 220),
                new Color(30, 160, 30, 240));
        helpButton.addActionListener(e -> {
            frame.remove(this);
            frame.openHelp();
            frame.revalidate();
            frame.repaint();
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(helpButton, gbc);

        // Settings button
        SpaceButton settingsButton = new SpaceButton("SETTINGS");
        settingsButton.setColors(new Color(70, 130, 180, 200), new Color(90, 150, 200, 220),
                new Color(50, 110, 160, 240));
        settingsButton.addActionListener(e -> {
            frame.remove(this);
            frame.openSettings();
            frame.revalidate();
            frame.repaint();
        });
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(settingsButton, gbc);

        // Exit button
        SpaceButton exitButton = new SpaceButton("EXIT GAME");
        exitButton.setColors(new Color(100, 50, 50, 200), new Color(130, 70, 70, 220), new Color(80, 30, 30, 240));
        exitButton.addActionListener(e -> {
            System.exit(0);
        });
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(exitButton, gbc);
    }

    private void createDifficultyButton(String text, Difficulty difficulty, Color baseColor,
            Color hoverColor, Color pressedColor,
            GridBagConstraints gbc, int x, int y) {
        SpaceButton button = new SpaceButton(text);
        button.setColors(baseColor, hoverColor, pressedColor);
        button.addActionListener(e -> {
            frame.remove(this);
            frame.openGameMenu(difficulty);
            frame.revalidate();
            frame.repaint();
        });

        gbc.gridx = x;
        gbc.gridy = y;
        add(button, gbc);
    }

    private void startAnimation() {
        animationTimer = new Timer(50, e -> {
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Use BackgroundArtist to render the space background with stars
        backgroundArtist.renderBackground(g2d);

        g2d.dispose();
    }

    // Stop animation when component is removed
    public void cleanup() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
