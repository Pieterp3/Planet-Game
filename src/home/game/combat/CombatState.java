package home.game.combat;

import home.game.Ship;

public class CombatState {
    Ship combatTarget;
    long lastShotTime;
    boolean inCombat;

    CombatState() {
        this.combatTarget = null;
        this.lastShotTime = 0;
        this.inCombat = false;
    }
}
