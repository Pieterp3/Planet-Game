package home.game.abilities;

import home.game.operators.Operator;

public class BlackHole {
    public final double x;
    public final double y;
    public final int eventHorizon;
    public final long endTime;
    public final Operator owner;
    public double rotationAngle = 0;

    public BlackHole(double x, double y, int eventHorizon, long endTime, Operator owner) {
        this.x = x;
        this.y = y;
        this.eventHorizon = eventHorizon;
        this.endTime = endTime;
        this.owner = owner;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }

    public Operator getOperator() {
        return owner;
    }
}