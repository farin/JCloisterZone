package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Collections;
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

        Class<? extends Meeple> meepleType = flierCap.getMeepleType();

        if (target == null || !game.isDeployAllowed(target, meepleType)) {
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

        MeepleAction action = null;
        if (meepleType.equals(SmallFollower.class)) {
            if (!getActivePlayer().hasFollower(SmallFollower.class)) {
                next();
                return;
            }
            action = new MeepleAction(SmallFollower.class, sites);
        } else {
            //tricky impl - first prepare unfiltered actions - TODO implement in better way
            List<PlayerAction> actions = new ArrayList<>();
            game.prepareFollowerActions(actions, sites);
            for (PlayerAction a : actions) {
                MeepleAction ma = (MeepleAction) a;
                if (ma.getMeepleType().equals(meepleType)) {
                    action = ma;
                    break;
                }
            }
        }
        if (action == null || action.getLocationsMap().isEmpty()) {
            next();
            return;
        }

        notifyUI(Collections.<PlayerAction>singletonList(action), false);
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    @Override
    public void next() {
        flierCap.setFlierDistance(null, 0);
        super.next();
    }

    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        if (!meepleType.equals(flierCap.getMeepleType())) {
            throw new IllegalArgumentException("Invalid meeple type.");
        }
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        Tile tile = getBoard().get(p);
        m.deployUnchecked(tile, loc, tile.getFeature(loc));
        game.fireGameEvent().deployed(m);
        next();
    }

}
