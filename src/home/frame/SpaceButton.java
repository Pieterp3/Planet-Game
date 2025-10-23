package home.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import home.game.VisualSettings;
import home.sounds.Sound;

public class SpaceButton extends JButton {

    private boolean isHovered = false;
    private boolean isPressed = false;
    private Color baseColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;

    public SpaceButton(String text) {
        super(text);
        initButton();
    }

    public SpaceButton(String text, Color baseColor, Color hoverColor, Color pressedColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.pressedColor = pressedColor;
        initButton();
    }

    private void initButton() {
        // Default colors matching space theme
        if (baseColor == null)
            baseColor = new Color(40, 60, 120, 200);
        if (hoverColor == null)
            hoverColor = new Color(60, 80, 160, 220);
        if (pressedColor == null)
            pressedColor = new Color(20, 40, 100, 240);
        if (textColor == null)
            textColor = Color.WHITE;

        // Remove default button appearance
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        // Set font
        setFont(new Font("Arial", Font.BOLD, 16));
        setForeground(textColor);

        // Add mouse listeners for hover and press effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Play hover sound
                if (VisualSettings.getInstance().getSoundManager() != null) {
                    VisualSettings.getInstance().getSoundManager().play(Sound.HOVER_MENU_BUTTON);
                }

                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;

                // Play click sound
                if (VisualSettings.getInstance().getSoundManager() != null) {
                    VisualSettings.getInstance().getSoundManager().play(Sound.CLICK_MENU_BUTTON);
                }

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });

        // Set preferred size
        setPreferredSize(new Dimension(200, 50));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine button color based on state
        Color currentColor;
        if (isPressed) {
            currentColor = pressedColor;
        } else if (isHovered) {
            currentColor = hoverColor;
        } else {
            currentColor = baseColor;
        }

        // Draw button background with rounded corners
        g2d.setColor(currentColor);
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
        g2d.fill(roundedRect);

        // Draw border
        g2d.setColor(new Color(100, 150, 255, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(roundedRect);

        // Add subtle glow effect when hovered
        if (isHovered || isPressed) {
            g2d.setColor(new Color(100, 150, 255, 50));
            g2d.setStroke(new BasicStroke(4));
            g2d.draw(roundedRect);
        }

        g2d.dispose();

        // Paint the text
        super.paintComponent(g);
    }

    public void setColors(Color base, Color hover, Color pressed) {
        this.baseColor = base;
        this.hoverColor = hover;
        this.pressedColor = pressed;
        repaint();
    }

    public void setTextColor(Color color) {
        this.textColor = color;
        setForeground(color);
    }
}