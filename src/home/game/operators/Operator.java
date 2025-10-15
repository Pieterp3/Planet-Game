package home.game.operators;

import home.game.Game;
import home.game.Ship;

public class Operator {

    private Game game;

    public Operator(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void addShip(Ship ship) {
        game.addShip(ship);
    }

    public void removeShip(Ship ship) {
        game.removeShip(ship);
    }

}
