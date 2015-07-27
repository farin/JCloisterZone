package com.jcloisterzone.ai.choice;

import java.util.concurrent.atomic.AtomicReference;

import com.jcloisterzone.ai.GameRanking;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.wsio.RmiProxy;

public abstract class AiChoice {

    private final SavePoint savePoint;
    private final AtomicReference<AiChoice> previous;
    private double ranking;

    public AiChoice(AiChoice previous, SavePoint savePoint) {
        this.previous = new AtomicReference<>(previous);
        this.savePoint = savePoint;
        this.ranking = 0.0;
    }

    public abstract void perform(RmiProxy server);

    public void rankPartial(GameRanking gr, Game game) {
        //empty default
    }

    public double getChainRanking() {
        AiChoice prev = previous.get();
        return ranking + (prev == null ? 0.0 : prev.getChainRanking());
    }

    public AiChoice getPrevious() {
        return previous.get();
    }

    public void setPrevious(AiChoice previous) {
        this.previous.set(previous);
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

    //debug method
    public String chainToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(this);
        if (previous.get() != null) {
            sb.append(" << ");
            sb.append(previous.get().chainToString());
        }
        sb.append("]");
        return sb.toString();
    }
}
