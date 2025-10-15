package home.frame.mainmenu.shop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import home.game.abilities.AbilityType;
import home.game.operators.player.PlayerData;

public class AbilityButtonListener implements ActionListener {
    private final AbilityType abilityType;
    private final PlayerData playerData;
    private final ShopMenu shopMenu;

    // Reference to the method that refreshes the upgrade display

    public AbilityButtonListener(ShopMenu shopMenu, PlayerData playerData, AbilityType abilityType) {
        this.shopMenu = shopMenu;
        this.playerData = playerData;
        this.abilityType = abilityType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (playerData.purchaseAbility(abilityType)) {
            shopMenu.refreshUpgrades(); // This will refresh the ability cards since we're using the same method

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
