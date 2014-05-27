package com.jcloisterzone.ai.choice;

import com.jcloisterzone.ai.GameRanking;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.Client2ClientIF;

public abstract class AiChoice {

    private final SavePoint savePoint;
    private AiChoice previous;
    private double ranking;

    public AiChoice(AiChoice previous, SavePoint savePoint) {
        this.previous = previous;
        this.savePoint = savePoint;
        this.ranking = 0.0;
    }

    public abstract void perform(Client2ClientIF server);

    public void rankPartial(GameRanking gr, Game game) {
        //empty default
    }

    public double getChainRanking() {
        return ranking + (previous == null ? 0.0 : previous.getChainRanking());
    }

    public AiChoice getPrevious() {
        return previous;
    }

    public void setPrevious(AiChoice previous) {
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
