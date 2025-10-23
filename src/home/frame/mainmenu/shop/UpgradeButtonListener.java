package home.frame.mainmenu.shop;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import home.game.operators.player.PlayerData;

import home.game.operators.player.UpgradeType;
import home.sounds.Sound;
import home.sounds.SoundManager;

public class UpgradeButtonListener implements ActionListener {

    private PlayerData playerData;
    private final UpgradeType upgradeType;
    private ShopMenu shopMenu;
    private SoundManager soundManager;

    public UpgradeButtonListener(UpgradeType upgradeType, PlayerData playerData, ShopMenu shopMenu,
            SoundManager soundManager) {
        this.upgradeType = upgradeType;
        this.playerData = playerData;
        this.shopMenu = shopMenu;
        this.soundManager = soundManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (playerData.purchaseUpgrade(upgradeType)) {
            shopMenu.refreshUpgrades();

            // Play success sound
            if (soundManager != null) {
                soundManager.play(Sound.PURCHASE_SUCCESS);
            }

            // Show purchase success feedback
            Timer feedbackTimer = new Timer(100, null);
            feedbackTimer.addActionListener(evt -> {
                // Could add particle effect here
                feedbackTimer.stop();
            });
            feedbackTimer.start();
        } else {
            // Play failure sound
            if (soundManager != null) {
                soundManager.play(Sound.PURCHASE_FAIL);
            }
        }
    }
}
