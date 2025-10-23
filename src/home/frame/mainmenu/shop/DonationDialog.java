package home.frame.mainmenu.shop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import home.frame.SpaceButton;
import home.frame.background.BackgroundArtist;
import home.game.VisualSettings;
import home.game.challenges.ChallengeManager;
import home.game.operators.player.PlayerData;
import home.sounds.Sound;

public class DonationDialog extends JDialog {
    private BackgroundArtist backgroundArtist;
    private PlayerData playerData;
    private boolean donationMade = false;
    private int[] donationAmounts = { 10, 50, 100, 500 };

    public DonationDialog(JFrame parent, PlayerData playerData) {
        super(parent, "Donate Gold", true);
        this.playerData = playerData;
        this.backgroundArtist = new BackgroundArtist();

        setupDialog();
        setupComponents();
    }

    private void setupDialog() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw space background
                backgroundArtist.renderBackground(g2d);

                // Draw semi-transparent overlay
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("GALACTIC RESEARCH FOUNDATION", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(100, 150, 255));

        JLabel subtitleLabel = new JLabel("Support Scientific Advancement", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitleLabel.setForeground(new Color(200, 200, 200));

        headerPanel.setLayout(new GridLayout(2, 1, 0, 5));
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JLabel infoLabel = new JLabel(
                "<html><center>Your generous donations help fund crucial research<br/>into planetary conquest technologies and space exploration.<br/><br/>Current Gold: <font color='#FFD700'>"
                        + playerData.getCoins() + "</font></center></html>",
                SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoLabel.setForeground(Color.WHITE);

        infoPanel.add(infoLabel);

        // Donation buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        for (int amount : donationAmounts) {
            SpaceButton donateBtn = new SpaceButton(amount + " COINS");
            donateBtn.setColors(new Color(255, 215, 0, 180), new Color(255, 235, 50, 200), new Color(200, 165, 0, 220));
            donateBtn.setPreferredSize(new Dimension(120, 50));

            donateBtn.addActionListener(e -> processDonation(amount));
            buttonPanel.add(donateBtn);
        }

        // Footer with cancel button
        JPanel footerPanel = new JPanel(new FlowLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        SpaceButton cancelBtn = new SpaceButton("CANCEL");
        cancelBtn.setColors(new Color(150, 50, 50, 180), new Color(180, 70, 70, 200), new Color(120, 30, 30, 220));
        cancelBtn.setPreferredSize(new Dimension(120, 40));
        cancelBtn.addActionListener(e -> dispose());

        footerPanel.add(cancelBtn);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Adjust layout
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(headerPanel);
        mainPanel.add(infoPanel);
        mainPanel.add(buttonPanel);
        mainPanel.add(footerPanel);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
    }

    private void processDonation(int amount) {
        if (playerData.spendCoins(amount)) {
            // Play donation sound
            if (VisualSettings.getGlobalSoundManager() != null) {
                VisualSettings.getGlobalSoundManager().play(Sound.GOLD_DONATION);
            }

            // Track the donation for challenges
            ChallengeManager.getInstance().onGoldDonated(amount);
            donationMade = true;

            // Show success dialog
            showSuccessDialog(amount);
            dispose();
        } else {
            // Show failure dialog
            showFailureDialog(amount);
        }
    }

    private void showSuccessDialog(int amount) {
        JDialog successDialog = new JDialog(this, "Donation Successful", true);
        successDialog.setSize(400, 200);
        successDialog.setLocationRelativeTo(this);
        successDialog.setUndecorated(true);
        successDialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                backgroundArtist.renderBackground(g2d);
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel(
                "<html><center><font color='#00FF00'>DONATION SUCCESSFUL!</font><br/><br/>Thank you for donating <font color='#FFD700'>"
                        + amount
                        + " coins</font>!<br/>The galactic research foundation appreciates your generosity.</center></html>",
                SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        SpaceButton okBtn = new SpaceButton("CONTINUE");
        okBtn.setColors(new Color(50, 150, 50, 180), new Color(70, 180, 70, 200), new Color(30, 120, 30, 220));
        okBtn.addActionListener(e -> successDialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        successDialog.add(panel);
        successDialog.setVisible(true);
    }

    private void showFailureDialog(int amount) {
        JDialog failDialog = new JDialog(this, "Insufficient Funds", true);
        failDialog.setSize(400, 200);
        failDialog.setLocationRelativeTo(this);
        failDialog.setUndecorated(true);
        failDialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                backgroundArtist.renderBackground(g2d);
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel(
                "<html><center><font color='#FF6666'>INSUFFICIENT FUNDS</font><br/><br/>You need <font color='#FFD700'>"
                        + amount + " coins</font> but only have <font color='#FFD700'>" + playerData.getCoins()
                        + " coins</font>.<br/>Play more games to earn additional funds!</center></html>",
                SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        SpaceButton okBtn = new SpaceButton("UNDERSTOOD");
        okBtn.setColors(new Color(150, 50, 50, 180), new Color(180, 70, 70, 200), new Color(120, 30, 30, 220));
        okBtn.addActionListener(e -> failDialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        failDialog.add(panel);
        failDialog.setVisible(true);
    }

    public boolean isDonationMade() {
        return donationMade;
    }
}
