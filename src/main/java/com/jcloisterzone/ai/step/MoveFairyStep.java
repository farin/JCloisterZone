package com.jcloisterzone.ai.step;

import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class MoveFairyStep extends Step {

    private final FairyAction action;
    private final Position pos;

    public MoveFairyStep(Step previous, SavePoint savePoint, FairyAction action, Position pos) {
        super(previous, savePoint);
        this.action = action;
        this.pos = pos;
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().moveFairy(pos);

    }

    @Override
    public void performOnServer(ServerIF server) {
        server.moveFairy(pos);

    }

    @Override
    public String toString() {
       return "move fairy to " + pos;
    }

    public FairyAction getAction() {
        return action;
    }

    public Position getPosition() {
        return pos;
    }
}
