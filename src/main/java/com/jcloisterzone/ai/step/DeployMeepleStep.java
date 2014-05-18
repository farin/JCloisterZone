package com.jcloisterzone.ai.step;

import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class DeployMeepleStep extends Step {
    private final Position pos;
    private final Location loc;
    private final SelectFeatureAction action;

    public DeployMeepleStep(Step previous, SavePoint savePoint, SelectFeatureAction action, Position pos, Location loc) {
        super(previous, savePoint);
        this.action = action;
        this.pos = pos;
        this.loc = loc;
    }

    private Class<? extends Meeple> getMeepleType() {
        if (action instanceof MeepleAction) {
            return ((MeepleAction)action).getMeepleType();
        }
        if (action instanceof BarnAction) {
            return Barn.class;
        }
        throw new IllegalArgumentException("Ünknown action type");
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().deployMeeple(pos, loc, getMeepleType());
    }

    @Override
    public void performOnServer(ServerIF server) {
        action.perform(server, pos, loc);
    }

    @Override
    public String toString() {
        return "deploy meeple " + getMeepleType().getSimpleName() + " on " + pos + " / " + loc;
    }

    public Position getPosition() {
        return pos;
    }

    public Location getLocation() {
        return loc;
    }

    public SelectFeatureAction getAction() {
        return action;
    }
}