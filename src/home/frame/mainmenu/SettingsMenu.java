package home.frame.mainmenu;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import home.frame.GameFrame;
import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;
import home.game.GameConstants;
import home.game.VisualSettings;
import home.game.abilities.AbilityType;
import home.game.operators.player.PlayerData;

public class SettingsMenu extends JPanel {

    private BackgroundArtist backgroundArtist;
    private GameFrame frame;
    private VisualSettings settings;

    // Animation variables
    private Timer animationTimer;

    // UI Components
    private JCheckBox connectionLinesCheckBox;
    private JSlider opacitySlider;
    private JCheckBox effectsCheckBox;
    private JCheckBox projectilesCheckBox;
    private JCheckBox planetMoonsCheckBox;
    private JCheckBox shipsCheckBox;
    private JLabel opacityValueLabel;
    private JComboBox<String> planetColorComboBox;

    public SettingsMenu(GameFrame frame) {
        this.frame = frame;
        this.backgroundArtist = new BackgroundArtist();
        this.settings = VisualSettings.getInstance();

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
        JLabel titleLabel = new JLabel("SETTINGS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(100, 150, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(titleLabel, gbc);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.setBackground(new Color(0, 0, 0, 0));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));

        // Make tab area semi-transparent so background shows through
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                if (isSelected) {
                    g2d.setColor(new Color(100, 150, 255, 120));
                } else {
                    g2d.setColor(new Color(50, 75, 125, 100));
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
        gbc.weighty = 0.8; // Reduce from 1.0 to 0.8 to leave space for buttons
        gbc.insets = new Insets(10, 50, 5, 50); // Reduce bottom inset
        add(tabbedPane, gbc);

        // Buttons Panel
        JPanel buttonsPanel = createButtonsPanel();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.weighty = 0.2; // Give some weight to ensure buttons are visible
        gbc.insets = new Insets(10, 10, 20, 10); // Reduce top inset, increase bottom
        add(buttonsPanel, gbc);
    }

    private JPanel createVisualSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
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

        JPanel opacityPanel = createOpacityPanel();
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
        panel.add(planetColorComboBox, gbc);

        // Reset to Defaults button
        SpaceButton resetButton = new SpaceButton("RESET DEFAULTS");
        resetButton.setColors(new Color(150, 100, 50, 200), new Color(180, 130, 70, 220), new Color(120, 80, 30, 240));
        resetButton.setPreferredSize(new Dimension(180, 44));
        resetButton.addActionListener(e -> {
            resetVisualSettingsToDefaults();
        });
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 8, 20); // Add more top margin to separate from other controls
        panel.add(resetButton, gbc);

        // Set preferred size to prevent the panel from growing too large
        panel.setPreferredSize(new Dimension(600, 350)); // Increase height slightly for the button
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        return panel;
    }

    private JPanel createKeybindsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());

        // Info label
        JLabel infoLabel = createStyledLabel("Configure keybinds for abilities. TAB toggles targeting lines.");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 14));
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
        // Set a maximum height to prevent the scroll pane from growing too large
        scrollPane.setPreferredSize(new Dimension(600, 300));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        populateKeybinds(keybindsContainer);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Reset keybinds button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        SpaceButton resetKeybindsButton = new SpaceButton("RESET KEYBINDS");
        resetKeybindsButton.setColors(new Color(150, 100, 50, 200), new Color(180, 130, 70, 220),
                new Color(120, 80, 30, 240));
        resetKeybindsButton.setPreferredSize(new Dimension(220, 44));
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
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.anchor = GridBagConstraints.WEST;

        PlayerData playerData = PlayerData.getInstance();

        int row = 0;

        // Show all available abilities
        for (AbilityType ability : AbilityType.values()) {
            if (!playerData.isAbilityUnlocked(ability)) {
                continue; // Skip locked abilities
            }

            // Ability icon and name
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel abilityLabel = createStyledLabel(ability.getDisplayName());
            abilityLabel.setFont(new Font("Arial", Font.BOLD, 16));
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
                    return 28;
                }

                @Override
                public int getIconHeight() {
                    return 28;
                }
            });
            abilityLabel.setIconTextGap(12);
            container.add(abilityLabel, gbc);

            // Current keybind or "None"
            Integer currentKey = settings.getKeyForAbility(ability);
            String keyText = currentKey != null ? VisualSettings.getKeyName(currentKey) : "None";

            gbc.gridx = 1;
            SpaceButton keybindButton = new SpaceButton(keyText);
            keybindButton.setPreferredSize(new Dimension(100, 38));
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
            clearButton.setPreferredSize(new Dimension(90, 36));
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
        JDialog keybindDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Keybind", true);
        keybindDialog.setSize(350, 180);
        keybindDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 30, 60));

        JLabel instructionLabel = new JLabel(
                "<html><center>Press a key (1-9, 0, -, =)<br/>for " + ability.getDisplayName() + "</center></html>");
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
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

    private JPanel createOpacityPanel() {
        JPanel opacityPanel = new JPanel();
        opacityPanel.setOpaque(false);
        opacityPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        opacitySlider = new JSlider(0, 100, (int) (settings.getConnectionLineOpacity() * 100));
        opacitySlider.setOpaque(false);
        opacitySlider.setForeground(Color.WHITE);
        opacitySlider.setPreferredSize(new Dimension(150, 25));
        opacitySlider.addChangeListener(e -> {
            float value = opacitySlider.getValue() / 100.0f;
            settings.setConnectionLineOpacity(value);
            opacityValueLabel.setText(String.format("%.0f%%", value * 100));
        });

        opacityValueLabel = createStyledLabel(String.format("%.0f%%", settings.getConnectionLineOpacity() * 100));
        opacityValueLabel.setPreferredSize(new Dimension(40, 20));

        opacityPanel.add(opacitySlider);
        opacityPanel.add(opacityValueLabel);
        return opacityPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        // Back to Main Menu button
        SpaceButton backButton = new SpaceButton("BACK TO MENU");
        backButton.setColors(new Color(100, 50, 50, 200), new Color(130, 70, 70, 220), new Color(80, 30, 30, 240));
        backButton.setPreferredSize(new Dimension(180, 50));
        backButton.addActionListener(e -> {
            frame.remove(this);
            frame.openMainMenu();
            frame.revalidate();
            frame.repaint();
        });

        buttonsPanel.add(backButton);
        return buttonsPanel;
    }

    private JCheckBox createStyledCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text, selected);
        checkBox.setOpaque(false);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Arial", Font.BOLD, 18));
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
        label.setFont(new Font("Arial", Font.BOLD, 16));
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
        comboBox.setFont(new Font("Arial", Font.BOLD, 14));
        comboBox.setPreferredSize(new Dimension(180, 30));

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
                            g2d.fillOval(x + 2, y + 2, 14, 14);
                            g2d.setColor(Color.WHITE);
                            g2d.drawOval(x + 2, y + 2, 14, 14);

                            g2d.dispose();
                        }

                        @Override
                        public int getIconWidth() {
                            return 18;
                        }

                        @Override
                        public int getIconHeight() {
                            return 18;
                        }
                    });
                }

                if (isSelected) {
                    setBackground(new Color(100, 150, 255, 120));
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

                // Draw border with space theme colors
                g2d.setColor(new Color(100, 150, 255, 200));
                g2d.drawRoundRect(x + 2, y + 2, 18, 18, 4, 4);

                // Fill background
                g2d.setColor(new Color(20, 30, 60, 150));
                g2d.fillRoundRect(x + 3, y + 3, 16, 16, 3, 3);

                if (selected) {
                    // Draw checkmark
                    g2d.setColor(new Color(100, 255, 100));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(x + 7, y + 11, x + 10, y + 14);
                    g2d.drawLine(x + 10, y + 14, x + 15, y + 8);
                }

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 22;
            }

            @Override
            public int getIconHeight() {
                return 22;
            }
        };
    }

    private void resetVisualSettingsToDefaults() {
        // Reset all visual settings to defaults
        settings.setDisplayConnectionLines(true);
        settings.setConnectionLineOpacity(1.0f);
        settings.setDisplayEffects(true);
        settings.setDisplayProjectiles(true);
        settings.setDisplayPlanetMoons(true);
        settings.setDisplayShips(true);
        settings.setPlayerPlanetColorByIndex(0); // Default to first color

        // Update UI components
        connectionLinesCheckBox.setSelected(settings.isDisplayConnectionLines());
        opacitySlider.setValue((int) (settings.getConnectionLineOpacity() * 100));
        opacityValueLabel.setText(String.format("%.0f%%", settings.getConnectionLineOpacity() * 100));
        effectsCheckBox.setSelected(settings.isDisplayEffects());
        projectilesCheckBox.setSelected(settings.isDisplayProjectiles());
        planetMoonsCheckBox.setSelected(settings.isDisplayPlanetMoons());
        shipsCheckBox.setSelected(settings.isDisplayShips());
        planetColorComboBox.setSelectedIndex(settings.getPlayerPlanetColorIndex());

        repaint();
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