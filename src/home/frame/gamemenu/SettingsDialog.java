package home.frame.gamemenu;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import home.frame.GameFrame;
import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;
import home.game.VisualSettings;
import home.game.abilities.AbilityType;
import home.game.operators.player.PlayerData;

public class SettingsDialog {
    private JDialog settingsDialog = null;
    private GameFrame frame;
    private BackgroundArtist backgroundArtist;
    private VisualSettings settings;

    // UI Components
    private JCheckBox connectionLinesCheckBox;
    private JSlider opacitySlider;
    private JCheckBox effectsCheckBox;
    private JCheckBox projectilesCheckBox;
    private JCheckBox planetMoonsCheckBox;
    private JCheckBox shipsCheckBox;
    private JLabel opacityValueLabel;
    private JComboBox<String> planetColorComboBox;

    public SettingsDialog(GameFrame frame) {
        this.frame = frame;
        this.backgroundArtist = new BackgroundArtist();
        this.settings = VisualSettings.getInstance();
    }

    /**
     * Shows the settings dialog with space theme aesthetic
     */
    public void show() {
        if (settingsDialog != null) {
            return; // Already showing
        }

        // Create custom settings dialog
        settingsDialog = new JDialog(frame, "Settings", true);
        settingsDialog.setSize(800, 700);
        settingsDialog.setLocationRelativeTo(frame);
        settingsDialog.setUndecorated(true);
        settingsDialog.setBackground(new Color(0, 0, 0, 0));

        // Create custom panel with space theme
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Draw background with space theme using BackgroundArtist
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
        Timer settingsAnimationTimer = new Timer(1000 / 60, e -> mainPanel.repaint());
        settingsAnimationTimer.start();

        // Stop animation when dialog is closed
        settingsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                settingsAnimationTimer.stop();
            }
        });

        mainPanel.setOpaque(false);
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        setupUI(mainPanel, gbc);

        settingsDialog.add(mainPanel);
        settingsDialog.setVisible(true);
    }

    private void setupUI(JPanel mainPanel, GridBagConstraints gbc) {
        // Title
        JLabel titleLabel = new JLabel("SETTINGS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(100, 150, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        // push title down so it doesn't intersect the custom rounded border drawn by
        // the background
        titleLabel.setBorder(BorderFactory.createEmptyBorder(22, 0, 8, 0));
        mainPanel.add(titleLabel, gbc);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.setBackground(new Color(0, 0, 0, 0));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        // Make tab area semi-transparent so background shows through
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                if (isSelected) {
                    g2d.setColor(new Color(100, 150, 255, 100));
                } else {
                    g2d.setColor(new Color(50, 75, 125, 80));
                }
                g2d.fillRoundRect(x, y, w, h, 8, 8);
                g2d.dispose();
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Paint semi-transparent border (30% opacity)
                g2d.setColor(new Color(100, 150, 255, 77)); // 77 = 30% of 255
                g2d.setStroke(new BasicStroke(2));

                // Calculate content area based on tab height
                int tabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                int contentY = (tabPlacement == TOP) ? tabHeight : 0;
                int contentHeight = tabbedPane.getHeight() - tabHeight;

                g2d.drawRoundRect(2, contentY, tabbedPane.getWidth() - 4, contentHeight - 2, 8, 8);
                g2d.dispose();
            }
        });

        // Visual Settings Tab
        JPanel visualPanel = createVisualSettingsPanel();
        tabbedPane.addTab("Visual", visualPanel);

        // Keybinds Tab
        JPanel keybindsPanel = createKeybindsPanel();
        tabbedPane.addTab("Keybinds", keybindsPanel);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 30, 10, 30);
        mainPanel.add(tabbedPane, gbc);

        // Back Button
        SpaceButton backButton = new SpaceButton("BACK");
        backButton.setColors(new Color(100, 50, 50, 200), new Color(130, 70, 70, 220), new Color(80, 30, 30, 240));
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> hide());
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(20, 10, 30, 10);
        mainPanel.add(backButton, gbc);
    }

    private JPanel createVisualSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 5, 20);
        gbc.anchor = GridBagConstraints.WEST;

        // Connection Lines Toggle
        connectionLinesCheckBox = createStyledCheckBox("Display Connection Lines", settings.isDisplayConnectionLines());
        connectionLinesCheckBox
                .addActionListener(e -> settings.setDisplayConnectionLines(connectionLinesCheckBox.isSelected()));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(connectionLinesCheckBox, gbc);

        // Connection Line Opacity
        JLabel opacityLabel = createStyledLabel("Connection Line Opacity:");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(opacityLabel, gbc);

        JPanel opacityPanel = new JPanel();
        opacityPanel.setOpaque(false);
        opacityPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        opacitySlider = new JSlider(0, 100, (int) (settings.getConnectionLineOpacity() * 100));
        opacitySlider.setOpaque(false);
        opacitySlider.setForeground(Color.WHITE);
        opacitySlider.addChangeListener(e -> {
            float value = opacitySlider.getValue() / 100.0f;
            settings.setConnectionLineOpacity(value);
            opacityValueLabel.setText(String.format("%.0f%%", value * 100));
        });

        opacityValueLabel = createStyledLabel(String.format("%.0f%%", settings.getConnectionLineOpacity() * 100));
        opacityValueLabel.setPreferredSize(new Dimension(40, 20));

        opacityPanel.add(opacitySlider);
        opacityPanel.add(opacityValueLabel);
        gbc.gridx = 1;
        panel.add(opacityPanel, gbc);

        // Effects Toggle
        effectsCheckBox = createStyledCheckBox("Display Effects", settings.isDisplayEffects());
        effectsCheckBox.addActionListener(e -> settings.setDisplayEffects(effectsCheckBox.isSelected()));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(effectsCheckBox, gbc);

        // Projectiles Toggle
        projectilesCheckBox = createStyledCheckBox("Display Projectiles", settings.isDisplayProjectiles());
        projectilesCheckBox.addActionListener(e -> settings.setDisplayProjectiles(projectilesCheckBox.isSelected()));
        gbc.gridy = 3;
        panel.add(projectilesCheckBox, gbc);

        // Planet Moons Toggle
        planetMoonsCheckBox = createStyledCheckBox("Display Planet Moons", settings.isDisplayPlanetMoons());
        planetMoonsCheckBox.addActionListener(e -> settings.setDisplayPlanetMoons(planetMoonsCheckBox.isSelected()));
        gbc.gridy = 4;
        panel.add(planetMoonsCheckBox, gbc);

        // Ships Toggle
        shipsCheckBox = createStyledCheckBox("Display Ships", settings.isDisplayShips());
        shipsCheckBox.addActionListener(e -> settings.setDisplayShips(shipsCheckBox.isSelected()));
        gbc.gridy = 5;
        panel.add(shipsCheckBox, gbc);

        // Planet Color Selection
        JLabel planetColorLabel = createStyledLabel("Planet Color:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(planetColorLabel, gbc);

        planetColorComboBox = createStyledComboBox();
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(planetColorComboBox, gbc);

        return panel;
    }

    private JPanel createKeybindsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());

        // Info label
        JLabel infoLabel = createStyledLabel("Configure keybinds for abilities. TAB toggles targeting lines.");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        panel.add(infoLabel, BorderLayout.NORTH);

        // Keybinds scroll panel
        JPanel keybindsContainer = new JPanel();
        keybindsContainer.setOpaque(false);
        keybindsContainer.setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(keybindsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        populateKeybinds(keybindsContainer);

        // Make the scroll pane background transparent so parent paint shows through
        scrollPane.setBackground(new Color(0, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Reset keybinds button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);

        SpaceButton resetKeybindsButton = new SpaceButton("RESET KEYBINDS");
        resetKeybindsButton.setColors(new Color(150, 100, 50, 200), new Color(180, 130, 70, 220),
                new Color(120, 80, 30, 240));
        resetKeybindsButton.setPreferredSize(new Dimension(220, 36));
        resetKeybindsButton.addActionListener(e -> {
            resetKeybindsToDefaults();
            keybindsContainer.removeAll();
            populateKeybinds(keybindsContainer);
            keybindsContainer.revalidate();
            keybindsContainer.repaint();
        });

        buttonPanel.add(resetKeybindsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void populateKeybinds(JPanel container) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        PlayerData playerData = PlayerData.getInstance();

        int row = 0;

        // Show all available abilities
        for (AbilityType ability : AbilityType.values()) {
            if (!playerData.isAbilityUnlocked(ability)) {
                continue; // Skip locked abilities
            }

            // Ability icon and name (drawn icon to match GameMenu style)
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel abilityLabel = createStyledLabel(ability.getDisplayName());
            abilityLabel.setIcon(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    int iconXshift = 0;
                    if (ability == AbilityType.CURSE) {
                        iconXshift = -10;
                    }
                    ability.drawIcon(g2d, x + getIconWidth() / 2 + iconXshift, y + getIconHeight() / 2,
                            Math.min(getIconWidth(), getIconHeight()) - 4);
                    g2d.dispose();
                }

                @Override
                public int getIconWidth() {
                    return 26;
                }

                @Override
                public int getIconHeight() {
                    return 26;
                }
            });
            abilityLabel.setIconTextGap(10);
            container.add(abilityLabel, gbc);

            // Current keybind or "None"
            Integer currentKey = settings.getKeyForAbility(ability);
            String keyText = currentKey != null ? VisualSettings.getKeyName(currentKey) : "None";

            gbc.gridx = 1;
            SpaceButton keybindButton = new SpaceButton(keyText);
            keybindButton.setPreferredSize(new Dimension(90, 28));
            keybindButton.setColors(
                    new Color(50, 70, 120, 200),
                    new Color(70, 90, 140, 220),
                    new Color(30, 50, 100, 240));

            // Set up keybind change listener
            keybindButton.addActionListener(e -> showKeybindDialog(ability, keybindButton));

            container.add(keybindButton, gbc);

            // Clear keybind button
            gbc.gridx = 2;
            SpaceButton clearButton = new SpaceButton("Clear");
            clearButton.setPreferredSize(new Dimension(90, 28));
            clearButton.setColors(
                    new Color(120, 50, 50, 200),
                    new Color(140, 70, 70, 220),
                    new Color(100, 30, 30, 240));
            clearButton.addActionListener(e -> {
                settings.clearKeybind(ability);
                keybindButton.setText("None");
            });

            container.add(clearButton, gbc);

            row++;
        }
    }

    private void showKeybindDialog(AbilityType ability, SpaceButton button) {
        JDialog keybindDialog = new JDialog(settingsDialog, "Set Keybind", true);
        keybindDialog.setSize(320, 160);
        keybindDialog.setLocationRelativeTo(settingsDialog);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 30, 60));

        JLabel instructionLabel = new JLabel("Press a key (1-9, 0, -, =) for " + ability.getDisplayName());
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        panel.add(instructionLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);

        SpaceButton cancelButton = new SpaceButton("Cancel");
        cancelButton.addActionListener(e -> keybindDialog.dispose());
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        keybindDialog.add(panel);

        // Add key listener to capture key press
        keybindDialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                // Only allow specific keys
                if (isValidKeybindKey(keyCode)) {
                    settings.setKeybind(keyCode, ability);
                    button.setText(VisualSettings.getKeyName(keyCode));
                    keybindDialog.dispose();
                }
            }
        });

        keybindDialog.setFocusable(true);
        keybindDialog.setVisible(true);
    }

    private boolean isValidKeybindKey(int keyCode) {
        return keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9 ||
                keyCode == KeyEvent.VK_0 ||
                keyCode == KeyEvent.VK_MINUS ||
                keyCode == KeyEvent.VK_EQUALS;
    }

    private void resetKeybindsToDefaults() {
        // Clear all keybinds first
        for (AbilityType ability : AbilityType.values()) {
            settings.clearKeybind(ability);
        }

        // Set default keybinds for unlocked abilities
        PlayerData playerData = PlayerData.getInstance();
        AbilityType[] abilities = AbilityType.values();
        int[] defaultKeys = {
                KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5,
                KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0,
                KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS
        };

        int keyIndex = 0;
        for (AbilityType ability : abilities) {
            if (playerData.isAbilityUnlocked(ability) && keyIndex < defaultKeys.length) {
                settings.setKeybind(defaultKeys[keyIndex], ability);
                keyIndex++;
            }
        }
    }

    private JCheckBox createStyledCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text, selected);
        checkBox.setOpaque(false);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Arial", Font.BOLD, 16));
        checkBox.setFocusPainted(false);

        // Custom checkbox appearance to match space theme
        checkBox.setIcon(createCheckBoxIcon(false));
        checkBox.setSelectedIcon(createCheckBoxIcon(true));
        checkBox.setRolloverIcon(createCheckBoxIcon(false));
        checkBox.setRolloverSelectedIcon(createCheckBoxIcon(true));

        return checkBox;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JComboBox<String> createStyledComboBox() {
        String[] colorNames = VisualSettings.PLANET_COLOR_NAMES;
        JComboBox<String> comboBox = new JComboBox<>(colorNames);

        // Set the current selection
        comboBox.setSelectedIndex(settings.getPlayerPlanetColorIndex());

        // Style the combo box
        comboBox.setOpaque(false);
        comboBox.setForeground(Color.WHITE);
        comboBox.setBackground(new Color(30, 30, 50, 200));
        comboBox.setFont(new Font("Arial", Font.BOLD, 12));
        comboBox.setPreferredSize(new Dimension(150, 25));

        // Create custom renderer to show color previews
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (index >= 0 && index < VisualSettings.AVAILABLE_PLANET_COLORS.length) {
                    Color planetColor = VisualSettings.AVAILABLE_PLANET_COLORS[index];

                    // Create a color preview icon
                    setIcon(new Icon() {
                        @Override
                        public void paintIcon(Component c, Graphics g, int x, int y) {
                            Graphics2D g2d = (Graphics2D) g.create();
                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                            // Draw planet color circle
                            g2d.setColor(planetColor);
                            g2d.fillOval(x + 2, y + 2, 12, 12);
                            g2d.setColor(Color.WHITE);
                            g2d.drawOval(x + 2, y + 2, 12, 12);

                            g2d.dispose();
                        }

                        @Override
                        public int getIconWidth() {
                            return 16;
                        }

                        @Override
                        public int getIconHeight() {
                            return 16;
                        }
                    });
                }

                if (isSelected) {
                    setBackground(new Color(100, 150, 255, 100));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(new Color(30, 30, 50, 200));
                    setForeground(Color.WHITE);
                }
                setOpaque(true);

                return this;
            }
        });

        // Add action listener to save color changes
        comboBox.addActionListener(e -> {
            int selectedIndex = comboBox.getSelectedIndex();
            settings.setPlayerPlanetColorByIndex(selectedIndex);
        });

        return comboBox;
    }

    private Icon createCheckBoxIcon(boolean selected) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw border
                g2d.setColor(new Color(100, 150, 255, 180));
                g2d.drawRoundRect(x + 2, y + 2, 16, 16, 4, 4);

                if (selected) {
                    // Draw checkmark
                    g2d.setColor(new Color(50, 255, 50));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(x + 6, y + 10, x + 9, y + 13);
                    g2d.drawLine(x + 9, y + 13, x + 14, y + 7);
                }

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }

    /**
     * Hides the settings dialog
     */
    public void hide() {
        if (settingsDialog != null) {
            settingsDialog.dispose();
            settingsDialog = null;
        }
    }

    /**
     * Checks if the settings dialog is currently visible
     */
    public boolean isVisible() {
        return settingsDialog != null && settingsDialog.isVisible();
    }
}