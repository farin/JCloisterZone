package com.jcloisterzone.ai.step;

import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class PlaceTowerPieceStep extends Step {

    private final TowerPieceAction action;
    private final Position pos;

    public PlaceTowerPieceStep(Step previous, SavePoint savePoint, TowerPieceAction action, Position pos) {
        super(previous, savePoint);
        this.action = action;
        this.pos = pos;
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().placeTowerPiece(pos);

    }

    @Override
    public void performOnServer(ServerIF server) {
        server.placeTowerPiece(pos);
    }

    @Override
    public String toString() {
       return "place tower piece on " + pos;
    }

    public TowerPieceAction getAction() {
        return action;
    }

    public Position getPosition() {
        return pos;
    }
}