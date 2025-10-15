package home.game;

import home.game.operators.Bot;
import home.game.operators.Difficulty;
import home.game.operators.Operator;
import home.game.operators.player.Player;
import home.game.planets.Planet;
import home.game.planets.PlanetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameGenerator {

    private Game game;
    private Random random;

    public GameGenerator(Game game) {
        this.game = game;
        this.random = new Random();
    }

    public void generate() {
        // Get difficulty settings
        Difficulty difficulty = game.getDifficulty();

        // Generate planets based on difficulty
        int minPlanets = difficulty.getMinPlanets();
        int maxPlanets = difficulty.getMaxPlanets();
        int numPlanets = random.nextInt(maxPlanets - minPlanets + 1) + minPlanets;

        // Generate bots based on difficulty
        int minBots = difficulty.getMinBots();
        int maxBots = difficulty.getMaxBots();
        int numBots = random.nextInt(maxBots - minBots + 1) + minBots;

        // Create operators
        List<Operator> operators = createOperators(numBots);

        // Generate planets with proper spacing
        List<Planet> planets = generatePlanets(numPlanets, operators);

        // Add planets to game
        for (Planet planet : planets) {
            game.addPlanet(planet);
        }
    }

    private List<Operator> createOperators(int numBots) {
        List<Operator> operators = new ArrayList<>();

        // Always add the player first
        Player player = game.getPlayer();
        operators.add(player);

        // Add bots
        for (int i = 0; i < numBots; i++) {
            Bot bot = new Bot(game);
            game.addBot(bot);
            operators.add(bot);
        }

        return operators;
    }

    private List<Planet> generatePlanets(int numPlanets, List<Operator> operators) {
        List<Planet> planets = new ArrayList<>();

        int defaultHealth = GameConstants.getMaxPlanetHealth() / 4 * 3;

        // Calculate planet ownership based on difficulty
        Difficulty difficulty = game.getDifficulty();
        double enemyRatio = difficulty.getEnemyPlanetRatio();
        int numBots = operators.size() - 1; // Subtract 1 for the player

        // Ensure player gets at least 1 planet, bots get according to ratio
        int playerPlanets = 1; // Player always gets at least 1
        int desiredEnemyPlanets = Math.max(numBots, (int) (numPlanets * enemyRatio));
        int neutralPlanets = Math.max(1, numPlanets - playerPlanets - desiredEnemyPlanets);

        // Adjust if we don't have enough planets
        if (playerPlanets + desiredEnemyPlanets + neutralPlanets > numPlanets) {
            neutralPlanets = numPlanets - playerPlanets - desiredEnemyPlanets;
            if (neutralPlanets < 0) {
                desiredEnemyPlanets = numPlanets - playerPlanets;
                neutralPlanets = 0;
            }
        }

        // Generate planets with orbital mechanics
        for (int i = 0; i < numPlanets; i++) {
            Operator owner = null;

            if (i < playerPlanets) {
                // First planet(s) go to player
                owner = game.getPlayer();
            } else if (i < playerPlanets + desiredEnemyPlanets) {
                // Next planets go to bots (distribute evenly)
                int botIndex = (i - playerPlanets) % numBots;
                owner = operators.get(botIndex + 1); // +1 because player is at index 0
            }
            // Remaining planets stay neutral (owner = null)

            PlanetType planetType = PlanetType.STANDARD;
            // Make harder difficulties more likely to have advanced planet types
            if (random.nextDouble() < difficulty.getAdvancedPlanetChance()) {
                planetType = PlanetType.getRandomPlanetType(difficulty);
            }

            // Create planet with some health variation
            int health = defaultHealth + random.nextInt(defaultHealth / 2) - defaultHealth / 4;
            health = Math.max(health, defaultHealth / 2); // Ensure minimum health

            // Generate orbital parameters (no collision checking needed with z-index
            // system)
            OrbitParameters orbitParams = OrbitParameters.generateOrbitParameters();

            // Create planet with orbital parameters
            Planet planet = new Planet(owner, health, planetType,
                    orbitParams.getSemiMajorAxis(), orbitParams.getSemiMinorAxis(),
                    orbitParams.getInitialAngle(), orbitParams.getOrbitalSpeed(),
                    orbitParams.isVerticalOrbit(), orbitParams.getZIndex());

            planets.add(planet);
        }

        return planets;
    }

}
