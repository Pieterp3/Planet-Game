package home.frame.mainmenu.achievements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import home.frame.GameFrame;
import home.frame.background.BackgroundArtist;
import home.game.challenges.Challenge;
import home.game.challenges.ChallengeManager;
import home.game.challenges.ChallengeType;
import home.game.operators.player.PlayerData;

public class AchievementMenu extends JPanel implements MouseListener {
    private GameFrame frame;
    private BackgroundArtist backgroundArtist;
    private ChallengeManager challengeManager;
    private PlayerData playerData;

    // UI Constants
    private static final int HEADER_HEIGHT = 120;
    private static final int CHALLENGE_HEIGHT = 80;
    private static final int CHALLENGE_MARGIN = 10;
    private static final int SCROLL_SPEED = 20;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;

    private CategoryFilter currentFilter = CategoryFilter.ALL;

    public AchievementMenu(GameFrame frame) {
        this.frame = frame;
        this.backgroundArtist = new BackgroundArtist();
        this.challengeManager = ChallengeManager.getInstance();
        this.playerData = PlayerData.getInstance();

        setLayout(null);
        setBackground(Color.BLACK);
        addMouseListener(this);
        setFocusable(true);

        // Enable mouse wheel scrolling
        addMouseWheelListener(e -> {
            scrollOffset += e.getWheelRotation() * SCROLL_SPEED;
            constrainScroll();
            repaint();
        });
    }

    private void constrainScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        backgroundArtist.renderBackground(g2d);

        // Draw challenges first (they will be clipped)
        drawChallenges(g2d);

        // Draw scrollbar if needed
        if (maxScrollOffset > 0) {
            drawScrollbar(g2d);
        }

        // Draw header last so it always appears on top
        drawHeader(g2d);
    }

    private void drawHeader(Graphics2D g2d) {
        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(new Color(100, 150, 255));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "ACHIEVEMENTS";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 50);

        // Achievement score
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.YELLOW);
        String scoreText = "Achievement Score: " + playerData.getAchievementScore();
        fm = g2d.getFontMetrics();
        int scoreX = (getWidth() - fm.stringWidth(scoreText)) / 2;
        g2d.drawString(scoreText, scoreX, 85);

        // Category filters
        drawCategoryFilters(g2d);

        // Back button
        drawBackButton(g2d);
    }

    private void drawCategoryFilters(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics fm = g2d.getFontMetrics();

        int startX = 50;
        int y = 110;
        int buttonWidth = 100;
        int buttonHeight = 25;
        int spacing = 10;

        for (CategoryFilter filter : CategoryFilter.values()) {
            // Button background
            if (filter == currentFilter) {
                g2d.setColor(new Color(100, 150, 255, 150));
            } else {
                g2d.setColor(new Color(50, 50, 50, 150));
            }
            g2d.fillRoundRect(startX, y - buttonHeight + 5, buttonWidth, buttonHeight, 10, 10);

            // Button border
            g2d.setColor(new Color(100, 150, 255));
            g2d.drawRoundRect(startX, y - buttonHeight + 5, buttonWidth, buttonHeight, 10, 10);

            // Button text
            g2d.setColor(Color.WHITE);
            String text = filter.getDisplayName();
            if (fm.stringWidth(text) > buttonWidth - 10) {
                text = text.substring(0, Math.min(text.length(), 8)) + "...";
            }
            int textX = startX + (buttonWidth - fm.stringWidth(text)) / 2;
            g2d.drawString(text, textX, y);

            startX += buttonWidth + spacing;
        }
    }

    private void drawBackButton(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(150, 50, 50, 200));
        g2d.fillRoundRect(getWidth() - 120, 20, 100, 30, 10, 10);

        g2d.setColor(new Color(200, 100, 100));
        g2d.drawRoundRect(getWidth() - 120, 20, 100, 30, 10, 10);

        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        String text = "BACK";
        int textX = getWidth() - 120 + (100 - fm.stringWidth(text)) / 2;
        g2d.drawString(text, textX, 40);
    }

    private void drawChallenges(Graphics2D g2d) {
        List<Challenge> challenges = getFilteredChallenges();

        // Calculate total content height first
        int totalContentHeight = challenges.size() * (CHALLENGE_HEIGHT + CHALLENGE_MARGIN);
        if (challenges.size() > 0) {
            totalContentHeight -= CHALLENGE_MARGIN; // Remove extra margin after last challenge
        }

        // Define bottom padding
        int bottomPadding = 50; // Reserve space at bottom

        // Calculate available height for content
        int availableHeight = getHeight() - HEADER_HEIGHT - bottomPadding;

        // Update max scroll offset - how much we need to scroll to see all content
        maxScrollOffset = Math.max(0, totalContentHeight - availableHeight);

        // Create a clipping region to prevent drawing over the header and reserve
        // bottom space
        Graphics2D clippedG2d = (Graphics2D) g2d.create();
        clippedG2d.clipRect(0, HEADER_HEIGHT, getWidth(), availableHeight);

        int y = HEADER_HEIGHT - scrollOffset;

        for (Challenge challenge : challenges) {
            // Draw all challenges that could be visible (let clipping handle the rest)
            if (y + CHALLENGE_HEIGHT > HEADER_HEIGHT - 50 && y < getHeight() + 50) {
                drawChallenge(clippedG2d, challenge, y);
            }
            y += CHALLENGE_HEIGHT + CHALLENGE_MARGIN;
        }

        // Dispose of the clipped graphics context
        clippedG2d.dispose();
    }

    private void drawChallenge(Graphics2D g2d, Challenge challenge, int y) {
        int x = 50;
        int width = getWidth() - 100;
        int height = CHALLENGE_HEIGHT - CHALLENGE_MARGIN;

        // Background
        Color bgColor = challenge.isCompleted() ? new Color(50, 100, 50, 150) : new Color(50, 50, 80, 150);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        // Border with rarity color
        g2d.setColor(challenge.getRarity().getColor());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 10, 10);
        g2d.setStroke(new BasicStroke(1));

        // Challenge name
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString(challenge.getName(), x + 15, y + 25);

        // Rarity indicator
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(challenge.getRarity().getColor());
        String rarityText = challenge.getRarity().name();
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(rarityText, x + width - fm.stringWidth(rarityText) - 15, y + 20);

        // Description
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(challenge.getDescription(), x + 15, y + 45);

        // Progress bar (if not completed and has target count)
        if (!challenge.isCompleted() && challenge.getTargetCount() > 0) {
            drawProgressBar(g2d, challenge, x + 15, y + 55, width - 200, 10);
        }

        // Reward info
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        if (challenge.isCompleted()) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("COMPLETED!", x + width - 100, y + 62);
        } else {
            g2d.setColor(Color.YELLOW);
            String rewardText = challenge.getCoinReward() + " coins + " +
                    challenge.getRarity().getScoreValue() + " score";
            g2d.drawString(rewardText, x + width - fm.stringWidth(rewardText) - 15, y + 45);
        }

        // Status icon
        if (challenge.isCompleted()) {
            g2d.setColor(Color.GREEN);
            int drawX = x + width - 20;
            int drawY = y + 50;
            g2d.fillOval(drawX, drawY, 15, 15);

            // Draw custom check mark
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));

            // Check mark coordinates (centered in the circle)
            int centerX = drawX + 7; // Center of the 15px circle
            int centerY = drawY + 7; // Center of the 15px circle

            // Draw check mark as two connected lines
            g2d.drawLine(centerX - 3, centerY, centerX - 1, centerY + 2); // Left part of check
            g2d.drawLine(centerX - 1, centerY + 2, centerX + 3, centerY - 2); // Right part of check

            // Reset stroke
            g2d.setStroke(new BasicStroke(1));
        }
    }

    private void drawProgressBar(Graphics2D g2d, Challenge challenge, int x, int y, int width, int height) {
        // Background
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRoundRect(x, y, width, height, 5, 5);

        // Progress
        float progress = challenge.getProgressPercentage();
        int progressWidth = (int) (width * progress);

        Color progressColor = progress > 0.8f ? Color.GREEN : progress > 0.5f ? Color.YELLOW : Color.RED;
        g2d.setColor(progressColor);
        g2d.fillRoundRect(x, y, progressWidth, height, 5, 5);

        // Progress text
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.WHITE);
        String progressText = challenge.getCurrentProgress() + " / " + challenge.getTargetCount();
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(progressText)) / 2;
        g2d.drawString(progressText, textX, y + 8);
    }

    private void drawScrollbar(Graphics2D g2d) {
        int scrollbarWidth = 10;
        int scrollbarX = getWidth() - scrollbarWidth - 5;
        int scrollbarY = HEADER_HEIGHT;
        int scrollbarHeight = getHeight() - HEADER_HEIGHT - 20;

        // Background
        g2d.setColor(new Color(100, 100, 100, 100));
        g2d.fillRoundRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 5, 5);

        // Thumb
        float scrollPercentage = (float) scrollOffset / maxScrollOffset;
        int thumbHeight = Math.max(20, scrollbarHeight / 3);
        int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * scrollPercentage);

        g2d.setColor(new Color(200, 200, 200, 150));
        g2d.fillRoundRect(scrollbarX, thumbY, scrollbarWidth, thumbHeight, 5, 5);
    }

    private List<Challenge> getFilteredChallenges() {
        List<Challenge> allChallenges = challengeManager.getAllChallenges();

        return allChallenges.stream()
                .filter(challenge -> {
                    switch (currentFilter) {
                        case ACTIVE:
                            return !challenge.isCompleted() && challenge.getCurrentProgress() > 0;
                        case COMPLETED:
                            return challenge.isCompleted();
                        case TIME_BASED:
                            return challenge.getType() == ChallengeType.COMPLETE_MISSION_TIME;
                        case ABILITIES:
                            return challenge.getType() == ChallengeType.USE_ABILITIES_COUNT ||
                                    challenge.getType() == ChallengeType.USE_SPECIFIC_ABILITY ||
                                    challenge.getType() == ChallengeType.PURCHASE_SPECIFIC_ABILITY;
                        case CONQUEST:
                            return challenge.getType() == ChallengeType.CAPTURE_PLANETS ||
                                    challenge.getType() == ChallengeType.WIN_WITHOUT_LOSING_PLANET ||
                                    challenge.getType() == ChallengeType.WIN_WITHOUT_CAPTURING_PLANET_TYPE;
                        case PROGRESSION:
                            return challenge.getType() == ChallengeType.UNLOCK_ABILITIES ||
                                    challenge.getType() == ChallengeType.PURCHASE_UPGRADES ||
                                    challenge.getType() == ChallengeType.DONATE_GOLD;
                        case ALL:
                        default:
                            return true;
                    }
                })
                .sorted((a, b) -> {
                    // Sort by completion status (active first), then by rarity
                    if (a.isCompleted() != b.isCompleted()) {
                        return a.isCompleted() ? 1 : -1;
                    }
                    return Integer.compare(b.getRarity().getScoreValue(), a.getRarity().getScoreValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        click(x, y);
    }

    private void click(int x, int y) {
        // Back button
        if (x >= getWidth() - 120 && x <= getWidth() - 20 && y >= 20 && y <= 50) {
            frame.remove(this);
            frame.openMainMenu();
            frame.revalidate();
            frame.repaint();
            return;
        }

        // Category filters
        if (y >= 85 && y <= 110) {
            int buttonWidth = 100;
            int spacing = 10;
            int startX = 50;

            for (int i = 0; i < CategoryFilter.values().length; i++) {
                int buttonStartX = startX + i * (buttonWidth + spacing);
                if (x >= buttonStartX && x <= buttonStartX + buttonWidth) {
                    currentFilter = CategoryFilter.values()[i];
                    scrollOffset = 0; // Reset scroll when changing filter
                    repaint();
                    return;
                }
            }
        }
    }

    private long pressedAt = 0;

    @Override
    public void mousePressed(MouseEvent e) {
        pressedAt = System.currentTimeMillis();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        long releasedAt = System.currentTimeMillis();
        if (releasedAt - pressedAt < 200) {
            click(e.getX(), e.getY());
        }
        pressedAt = 0;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}