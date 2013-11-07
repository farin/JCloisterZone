package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsCompleted;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FlierCapability;

public class FlierActionPhase extends Phase {

    private final FlierCapability flierCap;

    public FlierActionPhase(Game game) {
        super(game);
        flierCap = game.getCapability(FlierCapability.class);
    }

    @Override
    public void enter() {
        int distance = flierCap.getFlierDistance();
        Tile origin = game.getCurrentTile();
        Location direction = origin.getFlier().rotateCW(origin.getRotation());
        Position pos = game.getCurrentTile().getPosition();
        for (int i = 0; i < distance; i++) {
            pos = pos.add(direction);
        }
        Tile target = getBoard().get(pos);

        if (target == null || !game.isDeployAllowed(target, Follower.class)) {
            next();
            return;
        }

        LocationsMap sites = new LocationsMap();
        Set<Location> locations = new HashSet<>();
        for (Feature f : target.getFeatures()) {
            if (f instanceof Farm) continue;
            if (f.walk(new IsCompleted())) continue;
            locations.add(f.getLocation());
        }
        if (locations.isEmpty()) {
            next();
            return;
        }
        sites.put(pos, locations);

        List<PlayerAction> actions = new ArrayList<>();
        if (getActivePlayer().hasFollower(SmallFollower.class)) {
            actions.add(new MeepleAction(SmallFollower.class, sites));
        }
        game.prepareFollowerActions(actions, sites);
        notifyUI(actions, false);
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    @Override
    public void next() {
        flierCap.setFlierDistance(0);
        super.next();
    }

    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        Tile tile = getBoard().get(p);
        m.deployUnchecked(tile, loc, tile.getFeature(loc));
        game.fireGameEvent().deployed(m);
        next();
    }

}
