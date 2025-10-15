package home.frame.mainmenu.helpmenu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import home.frame.GameFrame;
import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;
import home.game.GameConstants;
import home.game.abilities.AbilityType;
import home.game.operators.Difficulty;
import home.game.operators.player.UpgradeType;

public class HelpMenu extends JPanel {

    private BackgroundArtist backgroundArtist;
    private GameFrame frame;
    private Timer animationTimer;

    // Content management
    private JScrollPane scrollPane;
    private JPanel contentPanel;

    // Section buttons
    private List<SpaceButton> sectionButtons;

    // Help sections
    private final String[] SECTIONS = {
            "OVERVIEW", "GAMEPLAY", "CONTROLS", "WALKTHROUGH",
            "UPGRADES", "ABILITIES", "SHOPPING", "CHALLENGES", "DIFFICULTY"
    };

    public HelpMenu(GameFrame frame) {
        this.frame = frame;
        this.backgroundArtist = new BackgroundArtist();
        this.sectionButtons = new ArrayList<>();

        // Enable tooltips
        ToolTipManager.sharedInstance().setEnabled(true);
        ToolTipManager.sharedInstance().setInitialDelay(500);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        setupLayout();
        setupUI();
        startAnimation();
        showSection("OVERVIEW");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(GameConstants.getGameWidth(), GameConstants.getGameHeight()));
    }

    private void setupUI() {
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Navigation panel (left side)
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.WEST);

        // Content panel (center with scrolling)
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(0, 0, 0, 180));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(new Color(40, 40, 40));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI());
        add(scrollPane, BorderLayout.CENTER);

        // Back button panel
        JPanel backPanel = createBackPanel();
        add(backPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JLabel titleLabel = new JLabel("HELP & TUTORIAL");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(100, 200, 255));
        headerPanel.add(titleLabel);

        return headerPanel;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        navPanel.setPreferredSize(new Dimension(240, 0));

        JLabel navTitle = new JLabel("SECTIONS");
        navTitle.setFont(new Font("Arial", Font.BOLD, 16));
        navTitle.setForeground(Color.WHITE);
        navTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        navPanel.add(navTitle);
        navPanel.add(Box.createVerticalStrut(15));

        // Create section buttons with tooltips
        String[] tooltips = {
                "Basic game overview and objectives",
                "Detailed game mechanics and systems",
                "Mouse and keyboard controls",
                "Interactive game demonstration",
                "Permanent upgrade system",
                "Special abilities and powers",
                "Economy and purchasing guide",
                "Achievement and challenge system",
                "Difficulty levels and scaling"
        };

        for (int i = 0; i < SECTIONS.length; i++) {
            String section = SECTIONS[i];
            SpaceButton sectionBtn = new SpaceButton(section);
            sectionBtn.setPreferredSize(new Dimension(220, 35));
            sectionBtn.setMaximumSize(new Dimension(220, 35));
            sectionBtn.setColors(
                    new Color(60, 80, 140, 180),
                    new Color(80, 100, 180, 200),
                    new Color(40, 60, 120, 220));
            sectionBtn.setToolTipText(tooltips[i]);

            sectionBtn.addActionListener(e -> {
                showSection(section);
                updateSectionButtons(section);
            });

            sectionButtons.add(sectionBtn);
            navPanel.add(sectionBtn);
            navPanel.add(Box.createVerticalStrut(8));
        }

        return navPanel;
    }

    private JPanel createBackPanel() {
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setOpaque(false);
        backPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        SpaceButton backButton = new SpaceButton("RETURN TO MAIN MENU");
        backButton.setPreferredSize(new Dimension(250, 40));
        backButton.setColors(
                new Color(100, 50, 50, 200),
                new Color(130, 70, 70, 220),
                new Color(80, 30, 30, 240));
        backButton.setToolTipText("Return to the main menu");
        backButton.addActionListener(e -> {
            cleanup();
            frame.remove(this);
            frame.openMainMenu();
            frame.revalidate();
            frame.repaint();
        });

        backPanel.add(backButton);
        return backPanel;
    }

    private void updateSectionButtons(String selectedSection) {
        for (int i = 0; i < sectionButtons.size(); i++) {
            SpaceButton btn = sectionButtons.get(i);
            String section = SECTIONS[i];

            if (section.equals(selectedSection)) {
                btn.setColors(
                        new Color(100, 150, 255, 200),
                        new Color(120, 170, 255, 220),
                        new Color(80, 130, 235, 240));
            } else {
                btn.setColors(
                        new Color(60, 80, 140, 180),
                        new Color(80, 100, 180, 200),
                        new Color(40, 60, 120, 220));
            }
            btn.repaint();
        }
    }

    private void showSection(String section) {
        contentPanel.removeAll();

        switch (section) {
            case "OVERVIEW":
                addOverviewContent();
                break;
            case "GAMEPLAY":
                addGameplayContent();
                break;
            case "CONTROLS":
                addControlsContent();
                break;
            case "WALKTHROUGH":
                addWalkthroughContent();
                break;
            case "UPGRADES":
                addUpgradesContent();
                break;
            case "ABILITIES":
                addAbilitiesContent();
                break;
            case "SHOPPING":
                addShoppingContent();
                break;
            case "CHALLENGES":
                addAchievementsContent();
                break;
            case "DIFFICULTY":
                addDifficultyContent();
                break;
        }

        contentPanel.revalidate();
        contentPanel.repaint();
        scrollPane.getVerticalScrollBar().setValue(0);
    }

    private void addOverviewContent() {
        addSectionTitle("Game Overview");
        addText("Welcome to Planet Conquest - a real-time strategy game where you command fleets of ships to capture and defend planets across the galaxy!");

        addSubtitle("▶ Objective");
        addText("Your goal is to capture ALL enemy planets while defending your own. The last player with planets remaining wins the battle!");

        addSubtitle("⚡ Core Mechanics");
        addBullet("Planets automatically spawn ships over time");
        addBullet("Ships travel to attack enemy planets or intercept enemy ships");
        addBullet("Captured planets start producing ships for you");
        addBullet("Use abilities and upgrades to gain strategic advantages");

        addSubtitle("★ Progression System");
        addText("Win battles to earn coins. Use coins to purchase permanent upgrades and unlock powerful abilities. Complete achievements for additional rewards and challenges.");
    }

    private void addGameplayContent() {
        addSectionTitle("Gameplay Mechanics");

        addSubtitle("● Planet System");
        addBullet("Planets have health (default max " + String.format("%,d", GameConstants.getMaxPlanetHealth())
                + ") and regenerate " + GameConstants.getPlanetHealthRegenRate() + " HP per second");

        // Format ship production rate more naturally
        double shipsPerSec = GameConstants.getDefaultShipsPerSecond();
        String shipProductionText;
        if (shipsPerSec >= 1.0) {
            shipProductionText = "Planets produce " + String.format("%.0f", shipsPerSec)
                    + " ships per second by default";
        } else {
            double secondsPerShip = 1.0 / shipsPerSec;
            shipProductionText = "Planets produce one ship every " + String.format("%.0f", secondsPerShip)
                    + " seconds by default";
        }
        addBullet(shipProductionText);

        addBullet("Each planet orbits the central star in elliptical paths");
        addBullet("Planet size is " + GameConstants.getPlanetSize() + " units for collision detection");

        addSubtitle("▲ Ship Behavior");
        addBullet("Ships automatically target enemies attacking their home planet");
        double speedPerSecond = GameConstants.getDefaultShipSpeed() * 60;
        String speedText = (speedPerSecond == Math.floor(speedPerSecond)) ? String.format("%.0f", speedPerSecond)
                : String.format("%.1f", speedPerSecond);
        addBullet("Ships have health (" + String.format("%,d", GameConstants.getDefaultShipHealth()) + "), damage ("
                + String.format("%,d", GameConstants.getDefaultShipDamage()) + "), and speed ("
                + speedText + " units/second)");
        addBullet("Ships engage in combat when within "
                + String.format("%.0f", GameConstants.getCombatEngagementDistance()) + " units of each other");
        addBullet("Ship collision radius is " + GameConstants.getShipSize() + " units");

        addSubtitle("⚔ Combat System");
        addBullet("Ships prioritize intercepting enemies over direct planet attacks");
        addBullet("Combat is resolved through projectile exchanges");
        double fireRate = 1000.0 / GameConstants.getShipFireRate();
        String fireRateText = (fireRate == Math.floor(fireRate)) ? String.format("%.0f", fireRate)
                : String.format("%.1f", fireRate);
        addBullet("Ships can fire " + fireRateText + " times per second");

        double projectileSpeed = GameConstants.getProjectileSpeed() * 60;
        String projectileSpeedText = (projectileSpeed == Math.floor(projectileSpeed))
                ? String.format("%.0f", projectileSpeed)
                : String.format("%.1f", projectileSpeed);
        addBullet("Projectiles travel at " + projectileSpeedText
                + " units per second with " + String.format("%.0f", GameConstants.getProjectileMaxRange())
                + " unit range");

        addSubtitle("◆ Real-Time Strategy");
        addBullet("Game runs at 60 ticks per second for smooth real-time action");
        addBullet("Multiple battles can occur simultaneously across the map");
        addBullet("Strategic timing of abilities is crucial for victory");
    }

    private void addControlsContent() {
        addSectionTitle("Game Controls");

        addSubtitle("▼ Mouse Controls");
        addBullet("LEFT CLICK on your planet: Select planet to send ships from");
        addBullet("LEFT CLICK on target: Send ships to attack that location");
        addBullet("DRAG to target: Slows down time for easier precise targeting");
        addBullet("HOVER over planets: View planet information and stats");
        addBullet("Navigation: Use mouse to interact with all menu elements");

        addSubtitle("■ Keyboard Shortcuts");
        addBullet("ESC: Cancel targeting operations and pause game");
        addBullet("P: Pause/Resume game");
        addBullet("TAB: Toggle targeting line visibility");

        addSubtitle("⚡ Ability Keybinds");
        addText("Activate abilities instantly with customizable keybinds:");
        addBullet("Default keys: 1-9, 0, -, = (first 12 unlocked abilities)");
        addBullet("Configure in Settings → Keybinds tab");
        addBullet("Only works for unlocked abilities");
        addBullet("Keybinds are automatically saved");

        addSubtitle("◯ Targeting System");
        addText("Ships use intelligent targeting:");
        addBullet("Priority 1: Intercept enemies attacking your planets");
        addBullet("Priority 2: Attack enemy planets directly");
        addBullet("Ships predict planet orbital positions for accurate targeting");

        addSubtitle("□ Visual Indicators");
        addBullet("Health bars are always visible below planets");
        addBullet("Targeting lines show ship destinations (press TAB to toggle)");
        addBullet("Planet colors indicate ownership and status");
        addBullet("Particle effects show active abilities and combat");
    }

    private void addWalkthroughContent() {
        addSectionTitle("Interactive Game Walkthrough");

        addText("Follow along with this step-by-step demonstration of a typical Planet Conquest game from start to victory!");

        // Create an interactive walkthrough panel
        WalkthroughPanel walkthroughPanel = new WalkthroughPanel();
        walkthroughPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        walkthroughPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(walkthroughPanel);

        addSubtitle("◆ Game Flow Summary");
        addBullet("1. Start with one planet producing ships every "
                + String.format("%.0f", 1.0 / GameConstants.getDefaultShipsPerSecond()) + " seconds");
        addBullet("2. Click your planet, then click an enemy planet to send an attack");
        addBullet("3. Capture enemy planets by depleting their health to zero");
        addBullet("4. Captured planets start producing ships for you automatically");
        addBullet("5. Use abilities strategically to gain advantages in crucial moments");
        addBullet("6. Continue until you control all planets to win the battle");

        addSubtitle("◯ Strategic Tips from the Demo");
        addBullet("Focus on expanding early - more planets = more ship production");
        addBullet("Defend your key planets while attacking weaker enemy positions");
        addBullet("Time your abilities carefully - they can turn the tide of battle");
        addBullet("Watch ship movements - they intercept enemies attacking your planets");
        addBullet("Patience wins battles - rushing leads to scattered, weak attacks");
    }

    private void addUpgradesContent() {
        addSectionTitle("Upgrade System");

        addSubtitle("$ Permanent Progression");
        addText("Upgrades are permanent improvements purchased with coins earned from victories. Each upgrade has 30 levels with exponentially increasing costs.");

        addSubtitle("↗ Available Upgrades");
        for (UpgradeType upgrade : UpgradeType.values()) {
            addUpgradeInfo(upgrade);
        }

        addSubtitle("! Upgrade Strategy Tips");
        addBullet("Early focus: Ship Spawn Speed and Ship Damage for faster expansion");
        addBullet("Mid-game: Planet Health and Damage Reduction for defense");
        addBullet("Late game: Ability Cooldown Reduction for frequent special attacks");
        addBullet("Balanced approach: Upgrade all stats gradually for well-rounded performance");

        addSubtitle("$ Cost Scaling");
        addText("Upgrade costs increase exponentially. Level 1 might cost ~10 coins, but Level 30 can cost over 400 coins. Plan your upgrade path carefully!");
    }

    private void addAbilitiesContent() {
        addSectionTitle("Special Abilities");

        addSubtitle("* Ability System");
        addText("Abilities are powerful temporary effects that can turn the tide of battle. They must be unlocked in the shop and have cooldown periods.");

        addSubtitle("+ Available Abilities");
        for (AbilityType ability : AbilityType.values()) {
            addAbilityInfo(ability);
        }

        addSubtitle("◐ Cooldown Management");
        addBullet("Each ability has a base cooldown period");
        addBullet("Ability Cooldown Reduction upgrade decreases all cooldowns");
        addBullet("Strategic timing is crucial - save powerful abilities for key moments");
        addBullet("Some abilities affect all your units, others target specific areas");

        addSubtitle("◯ Ability Strategy");
        addBullet("Defensive: Shield and Answered Prayers protect your forces");
        addBullet("Offensive: Missile Barrage and Black Hole damage enemies");
        addBullet("Economic: Factory Hype and Improved Factories boost production");
        addBullet("Control: Freeze and Curse weaken enemy capabilities");
    }

    private void addShoppingContent() {
        addSectionTitle("Shop & Economy");

        addSubtitle("▣ Shop Features");
        addBullet("Purchase permanent upgrades with coins");
        addBullet("Unlock new abilities for strategic advantages");
        addBullet("Donate coins for community challenges");
        addBullet("View upgrade costs and effects before purchasing");

        addSubtitle("$ Earning Coins");
        addText("Coins are earned by winning battles. The amount depends on difficulty:");
        addBullet("EASY: " + String.format("%.0f", Difficulty.EASY.getBotEfficiency() * 100) + "% coin multiplier ("
                + formatMultiplier(Difficulty.EASY.getBotEfficiency()) + " rewards)");
        addBullet("MEDIUM: " + String.format("%.0f", Difficulty.MEDIUM.getBotEfficiency() * 100) + "% coin multiplier ("
                + formatMultiplier(Difficulty.MEDIUM.getBotEfficiency()) + " rewards - baseline)");
        addBullet("HARD: " + String.format("%.0f", Difficulty.HARD.getBotEfficiency() * 100) + "% coin multiplier ("
                + formatMultiplier(Difficulty.HARD.getBotEfficiency()) + " rewards)");
        addBullet("EXTREME: " + String.format("%.0f", Difficulty.EXTREME.getBotEfficiency() * 100)
                + "% coin multiplier (" + formatMultiplier(Difficulty.EXTREME.getBotEfficiency()) + " rewards)");

        addSubtitle("◯ Spending Strategy");
        addBullet("Early game: Focus on Ship Spawn Speed and Damage");
        addBullet("Mid game: Invest in defensive upgrades and key abilities");
        addBullet("Late game: Max out Ability Cooldown and specialized upgrades");
        addBullet("Don't forget: Unlock abilities that match your playstyle");

        addSubtitle("♦ Donation System");
        addText("Some achievements require donating coins. This permanently removes coins from your account but contributes to special community challenges and shows your dedication to the game.");
    }

    private void addAchievementsContent() {
        addSectionTitle("Achievements & Challenges");

        addSubtitle("★ Achievement System");
        addText("Achievements track your progress and skill across multiple categories. Complete them to unlock rewards and demonstrate mastery!");

        addSubtitle("≡ Achievement Categories");
        addBullet("Victory Challenges: Win games under specific conditions");
        addBullet("Combat Challenges: Ship and planet battle achievements");
        addBullet("Economic Challenges: Coin earning and spending milestones");
        addBullet("Ability Challenges: Master the use of special abilities");
        addBullet("Time Challenges: Complete games within time limits");
        addBullet("Difficulty Challenges: Prove your skill on harder difficulties");

        addSubtitle("♦ Rarity System");
        addBullet("COMMON: Basic achievements for learning the game");
        addBullet("RARE: Moderate challenges requiring some skill");
        addBullet("EPIC: Difficult achievements for experienced players");
        addBullet("LEGENDARY: Extremely challenging feats for masters");

        addSubtitle("◯ Completion Benefits");
        addBullet("Track your progress and improvement over time");
        addBullet("Unlock bragging rights for difficult achievements");
        addBullet("Some achievements may unlock special features");
        addBullet("View detailed statistics about your gameplay");

        addSubtitle("↗ Progress Tracking");
        addText("Achievement progress is automatically saved and persistent across game sessions. Check the Achievements menu to see your current progress and what challenges await!");
    }

    private void addDifficultyContent() {
        addSectionTitle("Difficulty Levels");

        addSubtitle("≈ Difficulty Overview");
        addText("Choose your challenge level based on your skill and desired intensity. Higher difficulties offer greater rewards but much tougher opposition!");

        for (Difficulty diff : Difficulty.values()) {
            addSubtitle(diff.name().toUpperCase());
            addBullet("Bot Decision Time: " + String.format("%.1f", diff.getBotDecisionInterval() / 1000.0)
                    + " seconds (" + (diff == Difficulty.EASY ? "slow"
                            : diff == Difficulty.MEDIUM ? "moderate"
                                    : diff == Difficulty.HARD ? "quick" : "lightning")
                    + " responses)");
            addBullet("Bot Aggression: " + formatMultiplier(diff.getBotAggressiveness()) + " ("
                    + (diff == Difficulty.EASY ? "passive behavior"
                            : diff == Difficulty.MEDIUM ? "balanced behavior"
                                    : diff == Difficulty.HARD ? "aggressive behavior" : "highly aggressive")
                    + ")");
            addBullet("Coin Rewards: " + formatMultiplier(diff.getBotEfficiency()) + " ("
                    + (diff == Difficulty.EASY ? "reduced earnings"
                            : diff == Difficulty.MEDIUM ? "standard earnings"
                                    : diff == Difficulty.HARD ? "increased earnings" : "maximum earnings")
                    + ")");
            addBullet("Enemy Count: " + diff.getMinBots() + "-" + diff.getMaxBots() + " bots");
            addBullet("Planet Count: " + diff.getMinPlanets() + "-" + diff.getMaxPlanets() + " planets");
            addBullet("Enemy Planet Ratio: " + String.format("%.0f", diff.getEnemyPlanetRatio() * 100) + "%");
            addBullet("Bot Abilities: " + (diff.getBotsGetAbilities()
                    ? ("ENABLED" + (diff == Difficulty.EXTREME ? " - Advanced bot strategies!" : ""))
                    : "Disabled"));
        }

        addSubtitle("! Difficulty Tips");
        addBullet("Start with EASY to learn the mechanics");
        addBullet("MEDIUM provides the best balance of challenge and reward");
        addBullet("HARD and EXTREME require mastery of abilities and upgrades");
        addBullet("Higher difficulties dramatically increase bot intelligence");
    }

    // Helper method to format multipliers without unnecessary .0
    private String formatMultiplier(double value) {
        if (value == Math.floor(value)) {
            return String.format("%.0fx", value);
        } else {
            return String.format("%.1fx", value);
        }
    }

    // Helper methods for content creation
    private void addSectionTitle(String title) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(100, 200, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
    }

    private void addSubtitle(String subtitle) {
        JPanel subtitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw custom symbols
                String symbol = subtitle.substring(0, 1);
                drawCustomSymbol(g2d, symbol, 10, 15, 16);
                g2d.dispose();
            }
        };
        subtitlePanel.setOpaque(false);
        subtitlePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        subtitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("    " + subtitle.substring(2)); // Skip symbol and space
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        subtitleLabel.setForeground(new Color(255, 255, 150));

        subtitlePanel.add(subtitleLabel);
        contentPanel.add(subtitlePanel);
    }

    private void drawCustomSymbol(Graphics2D g2d, String symbol, int x, int y, int size) {
        g2d.setColor(new Color(255, 255, 150));
        int halfSize = size / 2;

        switch (symbol) {
            case "▶":
                // Triangle pointing right
                int[] triX = { x - halfSize / 2, x + halfSize, x - halfSize / 2 };
                int[] triY = { y - halfSize / 2, y, y + halfSize / 2 };
                g2d.fillPolygon(triX, triY, 3);
                break;
            case "⚡":
                // Lightning bolt
                int[] boltX = { x - 3, x + 1, x - 2, x + 4, x - 4, x };
                int[] boltY = { y - halfSize, y - 2, y, y + halfSize, y + 2, y };
                g2d.fillPolygon(boltX, boltY, 6);
                break;
            case "★":
                // Five-pointed star
                drawStar(g2d, x, y, halfSize, halfSize / 2);
                break;
            case "●":
                // Filled circle
                g2d.fillOval(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize);
                break;
            case "▲":
                // Triangle pointing up
                int[] upTriX = { x - halfSize / 2, x + halfSize / 2, x };
                int[] upTriY = { y + halfSize / 2, y + halfSize / 2, y - halfSize / 2 };
                g2d.fillPolygon(upTriX, upTriY, 3);
                break;
            case "⚔":
                // Crossed swords
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawLine(x - halfSize, y - halfSize, x + halfSize, y + halfSize);
                g2d.drawLine(x - halfSize, y + halfSize, x + halfSize, y - halfSize);
                // Hilts
                g2d.drawLine(x - halfSize + 2, y - halfSize - 2, x - halfSize - 2, y - halfSize + 2);
                g2d.drawLine(x + halfSize - 2, y + halfSize - 2, x + halfSize + 2, y + halfSize + 2);
                break;
            case "◆":
                // Diamond
                int[] diamX = { x, x + halfSize, x, x - halfSize };
                int[] diamY = { y - halfSize, y, y + halfSize, y };
                g2d.fillPolygon(diamX, diamY, 4);
                break;
            case "▼":
                // Triangle pointing down
                int[] downTriX = { x - halfSize / 2, x + halfSize / 2, x };
                int[] downTriY = { y - halfSize / 2, y - halfSize / 2, y + halfSize / 2 };
                g2d.fillPolygon(downTriX, downTriY, 3);
                break;
            case "■":
                // Square
                g2d.fillRect(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize);
                break;
            case "◯":
                // Circle outline
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawOval(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize);
                break;
            case "□":
                // Square outline
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawRect(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize);
                break;
            case "$":
                // Dollar sign
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawLine(x, y - halfSize, x, y + halfSize);
                // S curve
                g2d.drawArc(x - halfSize / 2, y - halfSize, halfSize, halfSize / 2, 0, 180);
                g2d.drawArc(x - halfSize / 2, y, halfSize, halfSize / 2, 180, 180);
                break;
            case "↗":
                // Arrow up-right
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawLine(x - halfSize, y + halfSize, x + halfSize, y - halfSize);
                g2d.drawLine(x + halfSize, y - halfSize, x + halfSize / 2, y - halfSize);
                g2d.drawLine(x + halfSize, y - halfSize, x + halfSize, y - halfSize / 2);
                break;
            case "!":
                // Exclamation mark
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawLine(x, y - halfSize, x, y);
                g2d.fillOval(x - 1, y + halfSize / 2, 2, 2);
                break;
            case "*":
                // Asterisk
                g2d.setStroke(new java.awt.BasicStroke(1.5f));
                g2d.drawLine(x, y - halfSize, x, y + halfSize);
                g2d.drawLine(x - halfSize, y, x + halfSize, y);
                g2d.drawLine(x - halfSize * 2 / 3, y - halfSize * 2 / 3, x + halfSize * 2 / 3, y + halfSize * 2 / 3);
                g2d.drawLine(x - halfSize * 2 / 3, y + halfSize * 2 / 3, x + halfSize * 2 / 3, y - halfSize * 2 / 3);
                break;
            case "+":
                // Plus sign
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawLine(x, y - halfSize, x, y + halfSize);
                g2d.drawLine(x - halfSize, y, x + halfSize, y);
                break;
            case "◐":
                // Half circle
                g2d.fillArc(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize, 270, 180);
                g2d.setStroke(new java.awt.BasicStroke(1.0f));
                g2d.drawOval(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize);
                break;
            case "▣":
                // Square with inner square
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawRect(x - halfSize / 2, y - halfSize / 2, halfSize, halfSize);
                g2d.fillRect(x - halfSize / 4, y - halfSize / 4, halfSize / 2, halfSize / 2);
                break;
            case "♦":
                // Diamond outline
                int[] outDiamX = { x, x + halfSize, x, x - halfSize };
                int[] outDiamY = { y - halfSize, y, y + halfSize, y };
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawPolygon(outDiamX, outDiamY, 4);
                break;
            case "≡":
                // Three horizontal lines
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawLine(x - halfSize, y - halfSize / 2, x + halfSize, y - halfSize / 2);
                g2d.drawLine(x - halfSize, y, x + halfSize, y);
                g2d.drawLine(x - halfSize, y + halfSize / 2, x + halfSize, y + halfSize / 2);
                break;
            case "≈":
                // Wavy lines
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                // Draw wavy line (approximated with arcs)
                g2d.drawArc(x - halfSize, y - halfSize / 4, halfSize / 2, halfSize / 2, 0, 180);
                g2d.drawArc(x - halfSize / 2, y - halfSize / 4, halfSize / 2, halfSize / 2, 180, 180);
                g2d.drawArc(x, y - halfSize / 4, halfSize / 2, halfSize / 2, 0, 180);
                g2d.drawArc(x - halfSize, y + halfSize / 4, halfSize / 2, halfSize / 2, 0, 180);
                g2d.drawArc(x - halfSize / 2, y + halfSize / 4, halfSize / 2, halfSize / 2, 180, 180);
                g2d.drawArc(x, y + halfSize / 4, halfSize / 2, halfSize / 2, 0, 180);
                break;
        }
    }

    private void drawStar(Graphics2D g2d, int x, int y, int outerRadius, int innerRadius) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * i / 5.0;
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = x + (int) (radius * Math.cos(angle - Math.PI / 2));
            yPoints[i] = y + (int) (radius * Math.sin(angle - Math.PI / 2));
        }

        g2d.fillPolygon(xPoints, yPoints, 10);
    }

    private void addText(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(0, 0, 0, 0));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(textArea);
    }

    private void addBullet(String text) {
        JTextArea bulletText = new JTextArea("• " + text);
        bulletText.setFont(new Font("Arial", Font.PLAIN, 14));
        bulletText.setForeground(new Color(200, 200, 200));
        bulletText.setBackground(new Color(0, 0, 0, 0));
        bulletText.setLineWrap(true);
        bulletText.setWrapStyleWord(true);
        bulletText.setEditable(false);
        bulletText.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 0));
        bulletText.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(bulletText);
    }

    private void addUpgradeInfo(UpgradeType upgrade) {
        JPanel upgradePanel = new JPanel(new BorderLayout());
        upgradePanel.setOpaque(false);
        upgradePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
        upgradePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("• " + upgrade.getDisplayName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(new Color(150, 255, 150));

        JLabel costLabel = new JLabel("Base Cost: " + upgrade.getBaseCost() + " coins");
        costLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        costLabel.setForeground(new Color(255, 215, 0));

        upgradePanel.add(nameLabel, BorderLayout.WEST);
        upgradePanel.add(costLabel, BorderLayout.EAST);

        // Force the panel to use its preferred width
        upgradePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, upgradePanel.getPreferredSize().height));

        contentPanel.add(upgradePanel);
    }

    private void addAbilityInfo(AbilityType ability) {
        JPanel abilityPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw ability icon
                int x = 35;
                if (ability == AbilityType.CURSE) {
                    x -= 10; // Center these wider icons better
                }
                ability.drawIcon(g2d, x, 15, 20);
                g2d.dispose();
            }
        };
        abilityPanel.setOpaque(false);
        abilityPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 8, 0));
        abilityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("            " + ability.getDisplayName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(new Color(255, 150, 255));

        JTextArea descArea = new JTextArea("                 " + ability.getDescription());
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setForeground(new Color(200, 200, 200));
        descArea.setBackground(new Color(0, 0, 0, 0));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);

        abilityPanel.add(nameLabel, BorderLayout.NORTH);
        abilityPanel.add(descArea, BorderLayout.CENTER);

        // Force the panel to use its preferred width
        abilityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, abilityPanel.getPreferredSize().height + 20));

        contentPanel.add(abilityPanel);
    }

    private void startAnimation() {
        animationTimer = new Timer(50, e -> repaint());
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Render animated background
        backgroundArtist.renderBackground(g2d);

        g2d.dispose();
    }

    public void cleanup() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

}