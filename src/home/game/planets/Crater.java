package home.game.planets;

// Crater data
public class Crater {
    public final double relativeX; // Position relative to planet center (-1 to 1)
    public final double relativeY;
    public final double size; // Relative to planet size (0.1 to 0.3)
    public final double depth; // Darkness factor (0.2 to 0.8)

    public Crater(double x, double y, double size, double depth) {
        this.relativeX = x;
        this.relativeY = y;
        this.size = size;
        this.depth = depth;
    }
}
