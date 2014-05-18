package com.jcloisterzone.ai.step;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class UndeployMeepleStep extends Step {

    private final Position pos;
    private final Location loc;
    private final Class<? extends Meeple> meepleType;
    private final Player meepleOwner;
    private final SelectFeatureAction action;

    public UndeployMeepleStep(Step previous, SavePoint savePoint, SelectFeatureAction action, Position pos, Location loc, Class<? extends Meeple> meepleType, Player meepleOwner) {
        super(previous, savePoint);
        this.pos = pos;
        this.loc = loc;
        this.meepleType = meepleType;
        this.meepleOwner = meepleOwner;
        this.action = action;
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().undeployMeeple(pos, loc, meepleType, meepleOwner.getIndex());
    }

    @Override
    public void performOnServer(ServerIF server) {
        server.undeployMeeple(pos, loc, meepleType, meepleOwner.getIndex());
    }

    @Override
    public String toString() {
        return "undeploy meeple " + meepleType.getSimpleName() + " on " + pos + " / " + loc;
    }

}
