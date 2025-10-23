package home.frame.mainmenu.shop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicScrollBarUI;

import home.frame.CoinIcon;
import home.frame.GameFrame;
import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;
import home.game.GameConstants;
import home.game.VisualSettings;
import home.game.abilities.AbilityType;
import home.game.operators.player.PlayerData;
import home.game.operators.player.UpgradeType;

public class ShopMenu extends JPanel {
    private BackgroundArtist backgroundArtist;
    private GameFrame frame;
    private PlayerData playerData;

    // UI Components
    private JLabel coinsLabel;
    private JScrollPane scrollPane;
    private JPanel upgradePanel;

    // Page management
    private boolean showingAbilities = false;
    private SpaceButton upgradesTabButton;
    private SpaceButton abilitiesTabButton;

    // Animation variables
    private Timer animationTimer;

    public ShopMenu(GameFrame frame) {
        this.frame = frame;
        this.backgroundArtist = new BackgroundArtist();
        this.playerData = PlayerData.getInstance();

        setupLayout();
        setupUI();
        startAnimation();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(GameConstants.getGameWidth(), GameConstants.getGameHeight()));
    }

    private void setupUI() {
        // Header panel with title and coins
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel with upgrades
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // Footer panel with back button
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();

        // Title
        JLabel titleLabel = new JLabel("UPGRADE SHOP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 42));
        titleLabel.setForeground(new Color(100, 150, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        headerPanel.add(titleLabel, gbc);

        // Coins display with custom painting
        coinsLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Font font = new Font("Arial", Font.BOLD, 24);
                String text = String.valueOf(playerData.getCoins());

                // Calculate position to center the coin + text
                int totalWidth = CoinIcon.getCoinWithTextWidth(g2d, text, font);
                int startX = (getWidth() - totalWidth) / 2 + font.getSize() / 2;
                int y = getHeight() / 2;

                CoinIcon.drawCoinWithText(g2d, startX, y, text, font, new Color(255, 215, 0));
                g2d.dispose();
            }
        };
        coinsLabel.setPreferredSize(new Dimension(200, 35));
        coinsLabel.setOpaque(false);
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        headerPanel.add(coinsLabel, gbc);

        // Tab buttons
        JPanel tabPanel = createTabPanel();
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 0, 10, 0);
        headerPanel.add(tabPanel, gbc);

        return headerPanel;
    }

    private JPanel createTabPanel() {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        tabPanel.setOpaque(false);

        upgradesTabButton = new SpaceButton("Upgrades");
        upgradesTabButton.setPreferredSize(new Dimension(120, 40));
        upgradesTabButton.addActionListener(e -> switchToUpgrades());

        abilitiesTabButton = new SpaceButton("Abilities");
        abilitiesTabButton.setPreferredSize(new Dimension(120, 40));
        abilitiesTabButton.addActionListener(e -> switchToAbilities());

        tabPanel.add(upgradesTabButton);
        tabPanel.add(abilitiesTabButton);

        // Set initial state
        updateTabAppearance();

        return tabPanel;
    }

    private void switchToUpgrades() {
        if (showingAbilities) {
            showingAbilities = false;
            updateTabAppearance();
            refreshUpgrades();
        }
    }

    private void switchToAbilities() {
        if (!showingAbilities) {
            showingAbilities = true;
            updateTabAppearance();
            refreshUpgrades();
        }
    }

    private void updateTabAppearance() {
        if (showingAbilities) {
            abilitiesTabButton.setBackground(new Color(0, 100, 200));
            upgradesTabButton.setBackground(new Color(60, 60, 60));
        } else {
            upgradesTabButton.setBackground(new Color(0, 100, 200));
            abilitiesTabButton.setBackground(new Color(60, 60, 60));
        }
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());

        // Create scrollable upgrade panel
        upgradePanel = new JPanel();
        upgradePanel.setOpaque(false);
        upgradePanel.setLayout(new GridBagLayout());

        populateUpgrades();

        // Create scroll pane with custom styling
        scrollPane = new JScrollPane(upgradePanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Style the scroll bar
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 150, 255, 150);
                this.trackColor = new Color(0, 0, 0, 100);
            }
        });

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private void populateUpgrades() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        if (showingAbilities) {
            // Show ability cards
            for (AbilityType abilityType : AbilityType.values()) {
                JPanel abilityCard = createAbilityCard(abilityType);
                gbc.gridy = row++;
                upgradePanel.add(abilityCard, gbc);
            }
        } else {
            // Show upgrade cards
            for (UpgradeType upgradeType : UpgradeType.values()) {
                JPanel upgradeCard = createUpgradeCard(upgradeType);
                gbc.gridy = row++;
                upgradePanel.add(upgradeCard, gbc);
            }
        }
    }

    private JPanel createUpgradeCard(UpgradeType upgradeType) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Enable antialiasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw semi-transparent background
                g2d.setColor(new Color(20, 30, 50, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Draw border
                g2d.setColor(new Color(100, 150, 255, 150));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2d.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Upgrade name
        JLabel nameLabel = new JLabel(upgradeType.getDisplayName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(nameLabel, gbc);

        // Current level and effect
        int currentLevel = playerData.getUpgradeLevel(upgradeType);
        double currentValue = playerData.getUpgradeValue(upgradeType);
        JLabel levelLabel = new JLabel(
                String.format("Level %d (+%.1f%s)", currentLevel, currentValue, upgradeType.getUnit()));
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        levelLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 1;
        card.add(levelLabel, gbc);

        // Next level preview
        double nextValue = upgradeType.calculateValue(currentLevel + 1);
        JLabel nextLabel = new JLabel(String.format("Next: +%.1f%s", nextValue, upgradeType.getUnit()));
        nextLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        nextLabel.setForeground(new Color(150, 200, 150));
        gbc.gridy = 2;
        card.add(nextLabel, gbc);

        // Purchase button
        int cost = playerData.getUpgradeCost(upgradeType);
        SpaceButton buyButton;

        if (cost == -1) {
            // Max level reached
            buyButton = new SpaceButton("MAX LEVEL");
            buyButton.setColors(new Color(100, 100, 100, 200), new Color(100, 100, 100, 200),
                    new Color(100, 100, 100, 200));
            buyButton.setEnabled(false);
        } else {
            // Create button with custom coin icon painting
            buyButton = new SpaceButton("") {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Font font = new Font("Arial", Font.BOLD, 12);
                    String text = String.valueOf(cost);

                    // Calculate position to center the coin + text
                    int totalWidth = CoinIcon.getCoinWithTextWidth(g2d, text, font);
                    int startX = (getWidth() - totalWidth) / 2 + font.getSize() / 2;
                    int y = getHeight() / 2;

                    CoinIcon.drawCoinWithText(g2d, startX, y, text, font, Color.WHITE);
                    g2d.dispose();
                }
            };
            boolean canAfford = playerData.getCoins() >= cost;
            if (canAfford) {
                buyButton.setColors(new Color(50, 150, 50, 200), new Color(70, 180, 70, 220),
                        new Color(30, 120, 30, 240));
            } else {
                buyButton.setColors(new Color(150, 50, 50, 200), new Color(150, 50, 50, 200),
                        new Color(150, 50, 50, 200));
                buyButton.setEnabled(false);
            }

            buyButton.addActionListener(
                    new UpgradeButtonListener(upgradeType, playerData, this, VisualSettings.getGlobalSoundManager()));
        }

        buyButton.setPreferredSize(new Dimension(120, 35));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(buyButton, gbc);

        return card;
    }

    private JPanel createAbilityCard(AbilityType abilityType) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Enable antialiasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw semi-transparent background with ability-specific color
                Color bgColor = playerData.isAbilityUnlocked(abilityType) ? new Color(30, 50, 30, 180)
                        : new Color(50, 30, 30, 180);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Draw border
                Color borderColor = playerData.isAbilityUnlocked(abilityType) ? new Color(100, 255, 100, 150)
                        : new Color(255, 150, 100, 150);
                g2d.setColor(borderColor);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2d.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ability icon and name
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        headerPanel.setOpaque(false);

        // Custom icon panel
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                abilityType.drawIcon(g2d, getWidth() / 2, getHeight() / 2, Math.min(getWidth(), getHeight()) - 4);
                g2d.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(24, 24));
        headerPanel.add(iconPanel);

        JLabel nameLabel = new JLabel(abilityType.getDisplayName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        headerPanel.add(nameLabel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(headerPanel, gbc);

        // Description
        JLabel descLabel = new JLabel(abilityType.getDescription());
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 1;
        card.add(descLabel, gbc);

        // Current level and effect or unlock status
        boolean isUnlocked = playerData.isAbilityUnlocked(abilityType);
        JLabel statusLabel;

        if (isUnlocked) {
            int currentLevel = playerData.getAbilityLevel(abilityType);
            String effectText = "";

            if (abilityType == AbilityType.MISSILE_BARRAGE) {
                effectText = String.format("Missiles: %d", playerData.getAbilityPower(abilityType));
            } else if (abilityType == AbilityType.ANSWERED_PRAYERS) {
                effectText = String.format("Healing: %d%%", Math.min(100, playerData.getAbilityPower(abilityType)));
            } else if (abilityType == AbilityType.ORBITAL_FREEZE) {
                effectText = String.format("Frozen Planets: %d | Duration: %.1fs",
                        playerData.getAbilityPower(abilityType), playerData.getAbilityDuration(abilityType));
            } else {
                effectText = String.format("Duration: %.1fs", playerData.getAbilityDuration(abilityType));
            }

            statusLabel = new JLabel(String.format("Level %d - %s", currentLevel, effectText));
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            statusLabel.setForeground(new Color(150, 200, 150));
        } else {
            statusLabel = new JLabel("LOCKED - Purchase to unlock");
            statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            statusLabel.setForeground(new Color(200, 150, 150));
        }

        gbc.gridy = 2;
        card.add(statusLabel, gbc);

        // Purchase/Upgrade button
        int cost = playerData.getAbilityCost(abilityType);
        SpaceButton buyButton;

        if (cost == -1) {
            // Max level reached
            buyButton = new SpaceButton("MAX LEVEL");
            buyButton.setColors(new Color(100, 100, 100, 200), new Color(100, 100, 100, 200),
                    new Color(100, 100, 100, 200));
            buyButton.setEnabled(false);
        } else {
            String buttonText = isUnlocked ? "UPGRADE" : "UNLOCK";
            buyButton = new SpaceButton("") {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw button text and coin
                    Font font = new Font("Arial", Font.BOLD, 11);
                    String text = buttonText + " " + cost;

                    int totalWidth = CoinIcon.getCoinWithTextWidth(g2d, text, font) + 20;
                    int startX = (getWidth() - totalWidth) / 2 + font.getSize() / 2;
                    int y = getHeight() / 2;

                    CoinIcon.drawCoinWithText(g2d, startX, y, text, font, Color.WHITE);
                    g2d.dispose();
                }
            };

            boolean canAfford = playerData.getCoins() >= cost;
            if (canAfford) {
                buyButton.setColors(new Color(50, 150, 50, 200), new Color(70, 180, 70, 220),
                        new Color(30, 120, 30, 240));
            } else {
                buyButton.setColors(new Color(150, 50, 50, 200), new Color(150, 50, 50, 200),
                        new Color(150, 50, 50, 200));
                buyButton.setEnabled(false);
            }

            buyButton.addActionListener(new AbilityButtonListener(this, playerData, abilityType));
        }

        buyButton.setPreferredSize(new Dimension(140, 35));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(buyButton, gbc);

        return card;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Donation button
        SpaceButton donateButton = new SpaceButton("DONATE GOLD");
        donateButton.setColors(new Color(255, 215, 0, 200), new Color(255, 235, 50, 220), new Color(200, 165, 0, 240));
        donateButton.setPreferredSize(new Dimension(200, 45));
        donateButton.addActionListener(e -> showDonationDialog());

        SpaceButton backButton = new SpaceButton("BACK TO MENU");
        backButton.setColors(new Color(150, 100, 50, 200), new Color(180, 130, 70, 220), new Color(120, 80, 30, 240));
        backButton.setPreferredSize(new Dimension(200, 45));
        backButton.addActionListener(e -> {
            frame.remove(this);
            frame.openMainMenu();
            frame.revalidate();
            frame.repaint();
        });

        footerPanel.add(donateButton);
        footerPanel.add(backButton);
        return footerPanel;
    }

    private void showDonationDialog() {
        DonationDialog dialog = new DonationDialog(frame, playerData);
        dialog.setVisible(true);

        if (dialog.isDonationMade()) {
            refreshUpgrades();
        }
    }

    public void refreshUpgrades() {
        upgradePanel.removeAll();
        populateUpgrades();
        coinsLabel.repaint(); // Repaint to update coin count
        revalidate();
        repaint();
    }

    private void startAnimation() {
        animationTimer = new Timer(50, e -> repaint());
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Use BackgroundArtist to render the space background
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