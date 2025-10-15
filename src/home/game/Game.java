package home.game;

import java.util.ArrayList;
import java.util.List;

import home.game.abilities.AbilityManager;
import home.game.challenges.ChallengeManager;
import home.game.combat.CombatManager;
import home.game.operators.Bot;
import home.game.operators.Difficulty;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.operators.player.PlayerData;
import home.game.planets.Planet;

public class Game {

    private Engine engine;
    private Player player;
    private List<Planet> planets;
    private List<Ship> ships;
    private List<Projectile> projectiles;
    private List<Explosion> explosions;
    private List<Bot> bots;
    private CombatManager combatManager;
    private AbilityManager abilityManager;
    private ChallengeManager challengeManager;
    private long gameStartTime;
    private boolean gameEnded = false;
    private Operator winner = null;
    private Difficulty difficulty;

    public Game() {
        this(Difficulty.MEDIUM); // Default to medium difficulty
    }

    public Game(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.engine = new Engine(this);
        this.player = new Player(this);
        this.planets = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.bots = new ArrayList<>();
        this.combatManager = new CombatManager(this);
        this.abilityManager = new AbilityManager(this);
        this.challengeManager = ChallengeManager.getInstance();
        this.gameStartTime = System.currentTimeMillis();

        // Start challenge tracking for this game session
        this.challengeManager.onNewGame(difficulty);
        // Initialize game entities
    }

    public void tick() {
        // Check for win condition first
        if (!gameEnded) {
            Operator currentWinner = checkWinCondition();
            if (currentWinner != null) {
                gameEnded = true;
                winner = currentWinner;

                // Track challenge progress if player won
                if (currentWinner == player) {
                    challengeManager.onGameWon();
                }

                return; // Stop the game
            }
        }

        // Update all planets
        for (Planet planet : planets) {
            planet.tick();
        }

        // Update combat manager (handles ship combat)
        combatManager.updateCombat();

        // Update ability manager (handles ability cooldowns and effects)
        abilityManager.update();

        // Update challenge manager for delayed saves
        challengeManager.updateSaveState();

        // Update all ships (movement only now)
        for (Ship ship : new ArrayList<>(ships)) {
            ship.tick();
        }

        // Update all projectiles
        for (Projectile projectile : new ArrayList<>(projectiles)) {
            projectile.tick();
        }

        // Update all explosions
        for (Explosion explosion : new ArrayList<>(explosions)) {
            explosion.tick();
            if (!explosion.isActive()) {
                explosions.remove(explosion);
            }
        }

        // Check for projectile-ship collisions
        checkProjectileCollisions();

        // Update all bots
        for (Bot bot : bots) {
            bot.tick();
        }
    }

    public void start() {
        new GameGenerator(this).generate();

        // Apply player upgrades to game entities
        PlayerData playerData = PlayerData.getInstance();
        playerData.applyUpgrades(this);

        engine.start();
    }

    public void stop() {
        // Force save any pending challenge progress before stopping
        challengeManager.forceSave();
        engine.stop();
    }

    public void pause() {
        engine.pause();
    }

    public void resume() {
        engine.resume();
    }

    public boolean isPaused() {
        return engine.isPaused();
    }

    public void addPlanet(Planet planet) {
        planet.setGame(this); // Set game reference for ability effects
        this.planets.add(planet);
    }

    public void addShip(Ship ship) {
        ship.setGame(this); // Set game reference for ability effects
        this.ships.add(ship);
    }

    public void addProjectile(Projectile projectile) {
        this.projectiles.add(projectile);
    }

    public void addExplosion(Explosion explosion) {
        this.explosions.add(explosion);
    }

    public List<Explosion> getExplosions() {
        return explosions;
    }

    public void addBot(Bot bot) {
        this.bots.add(bot);
    }

    public Player getPlayer() {
        return player;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Bot> getBots() {
        return bots;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public Engine getEngine() {
        return engine;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - gameStartTime;
    }

    public void removeShip(Ship ship) {
        this.ships.remove(ship);
        this.combatManager.removeShip(ship);
    }

    public void removeProjectile(Projectile projectile) {
        this.projectiles.remove(projectile);
    }

    public void removeBot(Bot bot) {
        this.bots.remove(bot);
    }

    /**
     * Checks for collisions between projectiles and ships
     */
    private void checkProjectileCollisions() {
        List<Projectile> projectilesToRemove = new ArrayList<>();
        List<Ship> shipsToRemove = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            if (!projectile.isActive()) {
                projectilesToRemove.add(projectile);
                continue;
            }

            // Check collision with all ships
            for (Ship ship : ships) {
                if (ship.isDestroyed())
                    continue;

                if (projectile.checkCollision(ship)) {
                    // Projectile hits ship
                    projectile.hitShip(ship);

                    // Create explosion at projectile hit location
                    addExplosion(new Explosion(projectile.getX(), projectile.getY(),
                            Explosion.ExplosionType.PROJECTILE_HIT));

                    projectilesToRemove.add(projectile);

                    // Check if ship is destroyed
                    if (ship.isDestroyed()) {
                        shipsToRemove.add(ship);
                    }
                    break; // Projectile can only hit one ship
                }
            }
        }

        // Remove inactive projectiles and destroyed ships
        projectiles.removeAll(projectilesToRemove);
        ships.removeAll(shipsToRemove);
    }

    /**
     * Checks win conditions:
     * 1. All planets belong to the same operator
     * 2. Player has no planets or ships left (loses)
     * 3. Player is the only operator left (wins)
     * 
     * @return The winning operator, or null if no winner yet
     */
    private Operator checkWinCondition() {
        if (planets.isEmpty()) {
            return null; // No planets, no winner
        }

        // Check if player has no planets or ships left
        boolean playerHasPlanets = false;
        boolean playerHasShips = false;

        for (Planet planet : planets) {
            if (planet.getOperator() == player) {
                playerHasPlanets = true;
                break;
            }
        }

        for (Ship ship : ships) {
            if (ship.getOperator() == player) {
                playerHasShips = true;
                break;
            }
        }

        // If player has no planets AND no ships, they lose
        if (!playerHasPlanets && !playerHasShips) {
            // Find any remaining operator to be the winner
            for (Planet planet : planets) {
                if (planet.getOperator() != null && planet.getOperator() != player) {
                    return planet.getOperator();
                }
            }
        }

        // Check if player is the only operator left
        boolean foundNonPlayerOperator = false;
        for (Planet planet : planets) {
            if (planet.getOperator() != null && planet.getOperator() != player) {
                foundNonPlayerOperator = true;
                break;
            }
        }

        // If player is the only operator left, player wins
        if (!foundNonPlayerOperator && playerHasPlanets) {
            return player;
        }

        // Original win condition: Check if all planets belong to the same operator
        Operator firstOperator = planets.get(0).getOperator();
        if (firstOperator == null) {
            return null; // First planet has no operator
        }

        // Check if all planets belong to the same operator
        for (Planet planet : planets) {
            if (planet.getOperator() != firstOperator) {
                return null; // Different operators found, no winner yet
            }
        }

        return firstOperator; // All planets belong to the same operator
    }

    /**
     * @return true if the game has ended (someone won)
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * @return the winning operator, or null if game hasn't ended
     */
    public Operator getWinner() {
        return winner;
    }

    /**
     * @return the game duration in milliseconds
     */
    public long getGameDuration() {
        return System.currentTimeMillis() - gameStartTime;
    }

    /**
     * Resets the game to initial state for a new game
     */
    public void resetGame() {
        // Stop the current engine first
        engine.stop();

        // Reset game state
        gameEnded = false;
        winner = null;
        gameStartTime = System.currentTimeMillis();
        planets.clear();
        ships.clear();
        projectiles.clear();
        bots.clear();

        // Reset ability cooldowns and effects
        abilityManager.resetAllAbilities();

        // Create a new engine
        engine = new Engine(this);

        // Re-initialize with new entities
        new GameGenerator(this).generate();
    }

    public boolean isStationedShip(Ship target) {
        for (Planet planet : planets) {
            if (planet.isShipStationed(target)) {
                return true;
            }
        }
        return false;
    }

}
