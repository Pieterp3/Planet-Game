package home.frame.mainmenu.shop;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import home.game.operators.player.PlayerData;

import home.game.operators.player.UpgradeType;

public class UpgradeButtonListener implements ActionListener {

    private PlayerData playerData;
    private final UpgradeType upgradeType;
    private ShopMenu shopMenu;

    public UpgradeButtonListener(UpgradeType upgradeType, PlayerData playerData, ShopMenu shopMenu) {
        this.upgradeType = upgradeType;
        this.playerData = playerData;
        this.shopMenu = shopMenu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (playerData.purchaseUpgrade(upgradeType)) {
            shopMenu.refreshUpgrades();

            // Show purchase success feedback
            Timer feedbackTimer = new Timer(100, null);
            feedbackTimer.addActionListener(evt -> {
                // Could add particle effect or sound here
                feedbackTimer.stop();
            });
            feedbackTimer.start();
        }
    }
}
