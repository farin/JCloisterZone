package com.jcloisterzone.ai.step;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class PlaceAbbeyStep extends Step {
    private final Position pos;
    private final AbbeyPlacementAction action;

    public PlaceAbbeyStep(Step previous, SavePoint savePoint, AbbeyPlacementAction action, Position pos) {
        super(previous, savePoint);
        this.action = action;
        this.pos = pos;
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().placeTile(Rotation.R0, pos);
    }

    @Override
    public void performOnServer(ServerIF server) {
        action.perform(server, pos);
    }

    @Override
    public String toString() {
        return "place Abbey tile on " + pos;
    }

    public Position getPosition() {
        return pos;
    }

    public AbbeyPlacementAction getAction() {
        return action;
    }
}