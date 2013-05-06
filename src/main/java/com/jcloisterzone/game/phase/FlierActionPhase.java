package com.jcloisterzone.game.phase;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsCompleted;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.FlierGame;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;

public class FlierActionPhase extends Phase {

    final FlierGame flierGame;

    public FlierActionPhase(Game game) {
        super(game);
        flierGame = game.getFlierGame();
    }

    @Override
    public void enter() {
        int distance = flierGame.getFlierDistance();
        Tile origin = game.getCurrentTile();
        Location direction = origin.getFlier().rotateCW(origin.getRotation());
        Position pos = game.getCurrentTile().getPosition();
        for (int i = 0; i < distance; i++) {
            pos = pos.add(direction);
        }
        Tile target = getBoard().get(pos);
        PrincessAndDragonGame pg = game.getPrincessAndDragonGame();

        if (target == null || (pg != null && pos.equals(pg.getDragonPosition()))) {
            next();
            return;
        }

        Sites sites = new Sites();
        Set<Location> locations = Sets.newHashSet();
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

        List<PlayerAction> actions = Lists.newArrayList();
        if (getActivePlayer().hasFollower(SmallFollower.class)) {
            actions.add(new MeepleAction(SmallFollower.class, sites));
        }
        game.expansionDelegate().prepareFollowerActions(actions, sites);
        notifyUI(actions, false);
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    @Override
    public void next() {
        flierGame.setFlierDistance(0);
        super.next();
    }

    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        Meeple m = getActivePlayer().getUndeployedMeeple(meepleType);
        Tile tile = getBoard().get(p);
        m.deployUnchecked(tile, loc, tile.getFeature(loc));
        game.fireGameEvent().deployed(m);
        next();
    }

}
