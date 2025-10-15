package home.frame;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class CoinIcon {
    private static long startTime = System.currentTimeMillis();
    private static final Color COIN_COLOR = new Color(255, 215, 0); // Gold color
    private static final Color COIN_EDGE = new Color(200, 165, 0);
    private static final Color COIN_HIGHLIGHT = new Color(255, 255, 200);

    /**
     * Draws a spinning coin icon at the specified position
     */
    public static void drawCoin(Graphics2D g, int x, int y, int size) {
        // Enable antialiasing for smooth coin
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate rotation for spinning effect
        long currentTime = System.currentTimeMillis();
        double rotationTime = (currentTime - startTime) / 1000.0;
        double angle = (rotationTime * 2.0) % (2 * Math.PI); // Complete rotation every 2 seconds

        // Calculate 3D effect - coin appears thinner when viewed from the side
        double perspective = Math.abs(Math.cos(angle));
        int coinWidth = (int) (size * perspective);
        int coinHeight = size;

        if (coinWidth < 3)
            coinWidth = 3; // Minimum width when viewed edge-on

        // Draw coin shadow
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x - coinWidth / 2 + 1, y - coinHeight / 2 + 1, coinWidth, coinHeight);

        // Draw main coin body
        g.setColor(COIN_COLOR);
        g.fillOval(x - coinWidth / 2, y - coinHeight / 2, coinWidth, coinHeight);

        // Draw coin edge/border
        g.setColor(COIN_EDGE);
        g.drawOval(x - coinWidth / 2, y - coinHeight / 2, coinWidth, coinHeight);

        // Add highlight for 3D effect
        if (perspective > 0.3) { // Only show details when coin is visible enough
            g.setColor(COIN_HIGHLIGHT);
            int highlightSize = Math.max(1, coinWidth / 4);
            g.fillOval(x - coinWidth / 4, y - coinHeight / 4, highlightSize, highlightSize);

            // Add a simple "$" symbol when coin is facing forward
            if (perspective > 0.7 && size > 10) {
                g.setColor(COIN_EDGE);
                g.setFont(new Font("Arial", Font.BOLD, size / 3));
                FontMetrics fm = g.getFontMetrics();
                String symbol = "$";
                int textX = x - fm.stringWidth(symbol) / 2;
                int textY = y + fm.getAscent() / 2 - 2;
                g.drawString(symbol, textX, textY);
            }
        }
    }

    /**
     * Draws a coin with text next to it
     */
    public static void drawCoinWithText(Graphics2D g, int x, int y, String text, Font font, Color textColor) {
        // Draw the coin
        int coinSize = font.getSize();
        drawCoin(g, x, y, coinSize);

        // Draw the text next to the coin
        g.setFont(font);
        g.setColor(textColor);
        FontMetrics fm = g.getFontMetrics();
        int textX = x + coinSize / 2 + 5;
        int textY = y + fm.getAscent() / 2 - 2;
        g.drawString(text, textX, textY);
    }

    /**
     * Gets the total width needed for coin + text
     */
    public static int getCoinWithTextWidth(Graphics2D g, String text, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int coinSize = font.getSize();
        return coinSize + 5 + fm.stringWidth(text);
    }
}