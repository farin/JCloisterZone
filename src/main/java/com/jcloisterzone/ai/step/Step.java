package com.jcloisterzone.ai.step;

import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public abstract class Step {
    private final SavePoint savePoint;
    private Step previous;
    private double ranking = 0.0;

    public Step(Step previous, SavePoint savePoint) {
        this.previous = previous;
        this.savePoint = savePoint;
    }

    public abstract void performLocal(Game game);
    public abstract void performOnServer(ServerIF server);

    public Step getPrevious() {
        return previous;
    }

    public void setPrevious(Step previous) {
        this.previous = previous;
    }

    public double getRanking() {
        return ranking;
    }

    public void setRanking(double ranking) {
        this.ranking = ranking;
    }

    public SavePoint getSavePoint() {
        return savePoint;
    }
}