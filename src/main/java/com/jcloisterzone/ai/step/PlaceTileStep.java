package com.jcloisterzone.ai.step;

import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.ai.GameRanking;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class PlaceTileStep extends Step {
    private final Rotation rot;
    private final Position pos;
    private final TilePlacementAction action; //note: don't use tile from action.getTile() - it is tile form original game, not copy

    public PlaceTileStep(Step previous, SavePoint savePoint, TilePlacementAction action, Rotation rot, Position pos) {
        super(previous, savePoint);
        this.action = action;
        this.rot = rot;
        this.pos = pos;
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().placeTile(rot, pos);
    }

    @Override
    public void performOnServer(ServerIF server) {
        action.perform(server, rot, pos);
    }

    @Override
    public void rankPartial(GameRanking gr, Game game) {
        Tile tile = game.getBoard().get(pos);
        this.setRanking(getRanking() + gr.getPartialAfterTilePlacement(game, tile));
    }

    @Override
    public String toString() {
        return "place " + action.getTile().getId() + " / " + rot + " tile on " + pos;
    }

    public Rotation getRotation() {
        return rot;
    }

    public Position getPosition() {
        return pos;
    }

    public TilePlacementAction getAction() {
        return action;
    }


}