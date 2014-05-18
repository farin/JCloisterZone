package com.jcloisterzone.ai.step;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class DeployMeepleStep extends Step {
    private final Position pos;
    private final Location loc;
    private final MeepleAction action;

    public DeployMeepleStep(Step previous, SavePoint savePoint, MeepleAction action, Position pos, Location loc) {
        super(previous, savePoint);
        this.action = action;
        this.pos = pos;
        this.loc = loc;
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().deployMeeple(pos, loc, action.getMeepleType());
    }

    @Override
    public void performOnServer(ServerIF server) {
        action.perform(server, pos, loc);
    }

    @Override
    public String toString() {
        return "deploy meeple " + action.getMeepleType().getSimpleName() + " on " + pos + " / " + loc;
    }

    public Position getPosition() {
        return pos;
    }

    public Location getLocation() {
        return loc;
    }

    public MeepleAction getAction() {
        return action;
    }
}