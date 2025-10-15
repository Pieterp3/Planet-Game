package home.frame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import home.frame.gamemenu.GameMenu;
import home.frame.mainmenu.MainMenu;
import home.frame.mainmenu.SettingsMenu;
import home.frame.mainmenu.achievements.AchievementMenu;
import home.frame.mainmenu.helpmenu.HelpMenu;
import home.frame.mainmenu.shop.ShopMenu;
import home.game.Game;
import home.game.GameConstants;
import home.game.challenges.ChallengeManager;
import home.game.operators.Difficulty;

public class GameFrame extends JFrame {

    private Game game;
    private MainMenu mainMenu;
    private GameMenu gameMenu;
    private ShopMenu shopMenu;
    private SettingsMenu settingsMenu;
    private HelpMenu helpMenu;

    public GameFrame(Game game) {
        super("Planet Conquest");
        this.game = game;
        setSize(GameConstants.getGameWidth() + 10, GameConstants.getGameHeight() + 30);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                ChallengeManager.getInstance().forceSave();
                System.exit(0);
            }
        });
        // setLocation(2000, 240);
        setLocationRelativeTo(null);
        setResizable(false);
        mainMenu = new MainMenu(this);
        add(mainMenu);
        setVisible(true);
    }

    private GameMenu createGameMenu(Difficulty difficulty) {
        gameMenu = new GameMenu(game, this);
        return gameMenu;
    }

    public void openGameMenu(Difficulty difficulty) {
        getContentPane().removeAll();
        game = new Game(difficulty); // Create fresh game instance with difficulty
        GameMenu menu = createGameMenu(difficulty);
        add(menu);
        revalidate();
        repaint();
    }

    // Keep the old method for backward compatibility
    public void openGameMenu() {
        openGameMenu(Difficulty.MEDIUM); // Default to medium difficulty
    }

    public void openMainMenu() {
        getContentPane().removeAll();
        // Clean up previous menu if it exists
        if (mainMenu != null) {
            mainMenu.cleanup();
        }
        mainMenu = new MainMenu(this);
        add(mainMenu);
        revalidate();
        repaint();
    }

    public void openShop() {
        getContentPane().removeAll();
        // Clean up previous menu if it exists
        if (shopMenu != null) {
            shopMenu.cleanup();
        }
        shopMenu = new ShopMenu(this);
        add(shopMenu);
        revalidate();
        repaint();
    }

    public void openAchievements() {
        getContentPane().removeAll();
        AchievementMenu achievementMenu = new AchievementMenu(this);
        add(achievementMenu);
        revalidate();
        repaint();
    }

    public void openSettings() {
        getContentPane().removeAll();
        // Clean up previous menu if it exists
        if (settingsMenu != null) {
            settingsMenu.cleanup();
        }
        settingsMenu = new SettingsMenu(this);
        add(settingsMenu);
        revalidate();
        repaint();
    }

    public void openHelp() {
        getContentPane().removeAll();
        // Clean up previous menu if it exists
        if (helpMenu != null) {
            helpMenu.cleanup();
        }
        helpMenu = new HelpMenu(this);
        add(helpMenu);
        revalidate();
        repaint();
    }

}
