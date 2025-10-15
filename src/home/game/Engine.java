package home.game;

public class Engine {

    private boolean paused, gameOver;
    private int ticksPerSecond = GameConstants.getTargetTPS();
    private boolean slowMode = false;
    private long lastTickTime;
    private long tickInterval = 1000 / ticksPerSecond;
    private Thread gameThread;
    private Game game;

    public Engine(Game game) {
        this.paused = false;
        this.gameOver = false;
        this.game = game;
    }

    public void start() {
        if (gameThread != null && gameThread.isAlive()) {
            return; // Engine is already running
        }
        paused = false;
        gameThread = new Thread(() -> {
            lastTickTime = System.currentTimeMillis();
            while (!gameOver) {
                if (paused) {
                    try {
                        Thread.sleep(GameConstants.getPauseSleepInterval()); // Sleep while paused
                    } catch (InterruptedException e) {
                        System.out.println("Engine thread interrupted");
                    }
                    continue;
                }
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTickTime >= tickInterval) {
                    // Update game state
                    lastTickTime = currentTime;
                    game.tick();
                }
                try {
                    Thread.sleep(GameConstants.getEngineCPUReliefSleep()); // Prevent CPU overload
                } catch (InterruptedException e) {
                    System.out.println("Engine thread interrupted 2");
                }
            }
        });
        gameThread.start();
    }

    public void stop() {
        gameOver = true;
        if (gameThread != null) {
            try {
                System.out.println("Waiting for engine thread to stop...");
                gameThread.join();
            } catch (InterruptedException e) {
                System.out.println("Engine thread join interrupted");
            }
        }
    }

    private void setTicksPerSecond(int ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.tickInterval = 1000 / ticksPerSecond;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public void enableSlowMode() {
        if (!slowMode) {
            slowMode = true;
            setTicksPerSecond(GameConstants.getSlowModeTPS());
        }
    }

    public void disableSlowMode() {
        if (slowMode) {
            slowMode = false;
            setTicksPerSecond(GameConstants.getTargetTPS());
        }
    }

    public boolean isSlowMode() {
        return slowMode;
    }
}
