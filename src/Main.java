import java.io.File;
import java.io.PrintStream;

import home.frame.GameFrame;
import home.game.Game;
import home.game.GameConstants;
import home.game.challenges.ChallengeManager;
import home.game.io.SaveLoadManager;

public class Main {

    private static Game game;
    private static GameFrame frame;
    private static File outputDir;

    public static void main(String[] args) throws Exception {
        if (GameConstants.printDebugToFile()) {
            outputDir = new File("logs.txt");
            if (!outputDir.exists()) {
                outputDir.createNewFile();
            }
            System.setOut(new PrintStream(outputDir));
        }

        // Initialize game constants from configuration file
        SaveLoadManager.getInstance().loadGameConstants();
        SaveLoadManager.getInstance().saveGameConstants();

        // Add shutdown hook to save challenge progress when program exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (game != null) {
                ChallengeManager.getInstance().forceSave();
            }
        }));

        game = new Game();
        frame = new GameFrame(game);
    }

    public static Game getGame() {
        return game;
    }

    public static GameFrame getFrame() {
        return frame;
    }
}
