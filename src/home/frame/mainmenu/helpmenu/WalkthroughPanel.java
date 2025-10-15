package home.frame.mainmenu.helpmenu;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import home.frame.SpaceButton;

public class WalkthroughPanel extends JPanel {
    private Timer demoTimer;
    private int currentStep = 0;
    private float animationProgress = 0.0f;
    private final int totalSteps = 8;

    // Demo state variables
    private final int PANEL_WIDTH = 600;
    private final int PANEL_HEIGHT = 400;

    // Simulated game entities for demo
    private DemoPlanet[] planets;
    private java.util.List<DemoShip> ships;
    private boolean isPaused = false;
    private SpaceButton playPauseBtn;

    public WalkthroughPanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(0, 0, 20));
        setBorder(BorderFactory.createLineBorder(new Color(100, 150, 255), 2));

        isPaused = false; // Start demo running
        initializeDemoEntities();
        setupDemoTimer();

        // Add control buttons
        setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setOpaque(false);

        playPauseBtn = new SpaceButton(isPaused ? "PLAY" : "PAUSE");
        playPauseBtn.setColors(new Color(50, 150, 50, 200), new Color(70, 170, 70, 220), new Color(30, 130, 30, 240));
        playPauseBtn.setToolTipText("Click to " + (isPaused ? "resume" : "pause") + " the walkthrough demo");
        playPauseBtn.addActionListener(e -> toggleDemo());

        SpaceButton restartBtn = new SpaceButton("RESTART");
        restartBtn.setColors(new Color(150, 150, 50, 200), new Color(170, 170, 70, 220), new Color(130, 130, 30, 240));
        restartBtn.setToolTipText("Click to restart the walkthrough demo from the beginning");
        restartBtn.addActionListener(e -> restartDemo());

        controlPanel.add(playPauseBtn);
        controlPanel.add(restartBtn);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void initializeDemoEntities() {
        // Create demo planets in strategic positions
        planets = new DemoPlanet[4];
        planets[0] = new DemoPlanet(100, 200, Color.BLUE, "Player Start", 10000); // Player planet
        planets[1] = new DemoPlanet(300, 150, Color.RED, "Enemy A", 8000); // Enemy 1
        planets[2] = new DemoPlanet(450, 250, Color.RED, "Enemy B", 6000); // Enemy 2
        planets[3] = new DemoPlanet(500, 100, Color.GRAY, "Neutral", 5000); // Neutral

        ships = new ArrayList<>();
    }

    private void setupDemoTimer() {
        demoTimer = new Timer(50, e -> {
            if (!isPaused) {
                updateDemo();
                repaint();
            }
        });
        demoTimer.start();
    }

    private void updateDemo() {
        animationProgress += 0.02f; // Faster progression

        // Progress through demo steps based on animation progress
        int newStep = Math.min((int) (animationProgress / 2.0f), totalSteps - 1); // Much faster step progression
        if (newStep != currentStep) {
            currentStep = newStep;
            executeStep(currentStep);
        }

        // Update ships
        for (int i = ships.size() - 1; i >= 0; i--) {
            DemoShip ship = ships.get(i);
            ship.update();
            if (ship.hasReachedTarget()) {
                ships.remove(i);
            }
        }

        // Auto-restart after completion
        if (animationProgress > totalSteps * 2.5f) { // Faster restart
            restartDemo();
        }
    }

    private void executeStep(int step) {
        switch (step) {
            case 0: // Initial setup explanation
                // Start with some ships immediately for visibility
                sendDemoShips(planets[0], planets[1], 2);
                break;
            case 1: // Player selects home planet
                sendDemoShips(planets[0], planets[1], 1);
                break;
            case 2: // Send ships to Enemy A
                sendDemoShips(planets[0], planets[1], 3);
                break;
            case 3: // Capture Enemy A
                planets[1].owner = Color.BLUE;
                planets[1].health = 4000;
                break;
            case 4: // Send ships from both planets to Enemy B
                sendDemoShips(planets[0], planets[2], 2);
                sendDemoShips(planets[1], planets[2], 2);
                break;
            case 5: // Capture Enemy B
                planets[2].owner = Color.BLUE;
                planets[2].health = 3000;
                break;
            case 6: // Attack neutral planet from multiple sources
                sendDemoShips(planets[0], planets[3], 1);
                sendDemoShips(planets[1], planets[3], 1);
                sendDemoShips(planets[2], planets[3], 2);
                break;
            case 7: // Victory - capture final planet
                planets[3].owner = Color.BLUE;
                planets[3].health = 2000;
                break;
        }
    }

    private void sendDemoShips(DemoPlanet from, DemoPlanet to, int count) {
        for (int i = 0; i < count; i++) {
            ships.add(new DemoShip(from.x, from.y, to.x, to.y, from.owner));
        }
    }

    private void toggleDemo() {
        isPaused = !isPaused;
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        if (playPauseBtn != null) {
            playPauseBtn.setText(isPaused ? "PLAY" : "PAUSE");
            playPauseBtn.setToolTipText("Click to " + (isPaused ? "resume" : "pause") + " the walkthrough demo");
        }
    }

    private void restartDemo() {
        currentStep = 0;
        animationProgress = 0.0f;
        ships.clear();
        initializeDemoEntities();
        isPaused = false;
        updatePlayPauseButton();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw space background
        drawStarField(g2d);

        // Draw planets
        for (DemoPlanet planet : planets) {
            planet.draw(g2d);
        }

        // Draw ships
        for (DemoShip ship : ships) {
            ship.draw(g2d);
        }

        // Draw step description
        drawStepDescription(g2d);

        g2d.dispose();
    }

    private void drawStarField(Graphics2D g2d) {
        // Simple star field background
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 50; i++) {
            int x = (int) (Math.sin(i * 1.7) * 200 + 300);
            int y = (int) (Math.cos(i * 2.3) * 150 + 200);
            g2d.fillOval(x % PANEL_WIDTH, y % PANEL_HEIGHT, 1, 1);
        }
    }

    private void drawStepDescription(Graphics2D g2d) {
        String[] stepTexts = {
                "Step 1: Game starts with your blue planet producing ships",
                "Step 2: Click your planet to select it for sending ships",
                "Step 3: Click enemy planet to send attack ships",
                "Step 4: Ships arrive and capture the enemy planet",
                "Step 5: Send ships from multiple planets for stronger attacks",
                "Step 6: Captured planets produce ships for you automatically",
                "Step 7: Coordinate multi-planet attacks on remaining enemies",
                "Step 8: Victory! You control all planets in the system"
        };

        if (currentStep < stepTexts.length) {
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.setFont(new Font("Arial", Font.BOLD, 14));

            // Draw text with background
            String text = stepTexts[currentStep];
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();

            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRoundRect(10, 10, textWidth + 20, textHeight + 10, 10, 10);

            g2d.setColor(new Color(100, 200, 255));
            g2d.drawString(text, 20, 25 + fm.getAscent());
        }

    }

    // Cleanup when panel is destroyed
    public void cleanup() {
        if (demoTimer != null) {
            demoTimer.stop();
        }
    }
}
