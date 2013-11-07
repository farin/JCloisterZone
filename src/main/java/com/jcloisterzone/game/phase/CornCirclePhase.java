package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.CornCircleCapability;
import com.jcloisterzone.game.capability.CornCircleCapability.CornCicleOption;

public class CornCirclePhase extends Phase {

    private final CornCircleCapability cornCircleCap;

    public CornCirclePhase(Game game) {
        super(game);
        cornCircleCap = game.getCapability(CornCircleCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(CornCircleCapability.class);
    }

    @Override
    public Player getActivePlayer() {
        Player player = cornCircleCap.getCornCirclePlayer();
        if (player == null) return super.getActivePlayer();
        return player;
    }

    @Override
    public void enter() {
        if (getTile().getCornCircle() == null) {
            next();
            return;
        }
        game.getUserInterface().selectCornCircleOption();
    }

    private void nextCornPlayer() {
        Player active = getActivePlayer();
        if (active == game.getTurnPlayer()) {
            cornCircleCap.setCornCirclePlayer(null);
            cornCircleCap.setCornCircleOption(null);
            next();
        } else {
            Player cornPlayer = game.getNextPlayer(active);
            cornCircleCap.setCornCirclePlayer(cornPlayer);
            prepareCornAction();
        }
    }

    @Override
    public void cornCiclesRemoveOrDeploy(boolean remove) {
        if (remove) {
            cornCircleCap.setCornCircleOption(CornCicleOption.REMOVAL);
        } else {
            cornCircleCap.setCornCircleOption(CornCicleOption.DEPLOYMENT);
        }
        Player cornPlayer = game.getNextPlayer(getActivePlayer());
        cornCircleCap.setCornCirclePlayer(cornPlayer);
        prepareCornAction();
    }

    private void prepareCornAction() {
        List<PlayerAction> actions;
        Class<? extends Feature> cornType = getTile().getCornCircle();
        if (cornCircleCap.getCornCircleOption() == CornCicleOption.REMOVAL) {
            actions = prepareRemovalAction(cornType);
        } else {
            actions = prepareDeploymentAction(cornType);
        }
        if (actions.isEmpty()) {
            nextCornPlayer();
        } else {
            notifyUI(actions, cornCircleCap.getCornCircleOption() == CornCicleOption.DEPLOYMENT);
        }
    }

    private List<PlayerAction> prepareDeploymentAction(Class<? extends Feature> cornType) {
        LocationsMap sites = new LocationsMap();
        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m instanceof Follower)) continue;
            if (m.getPlayer() != getActivePlayer()) continue;
            if (!cornType.isInstance(m.getFeature())) continue;
            sites.getOrCreate(m.getPosition()).add(m.getLocation());
        }
        if (sites.isEmpty()) return Collections.emptyList();

        List<PlayerAction> actions = new ArrayList<>();
        //TODO nice to do this in generic way independtly on particular followers enumeration
        if (getActivePlayer().hasFollower(SmallFollower.class)) {
            actions.add(new MeepleAction(SmallFollower.class, sites));
        }
        if (getActivePlayer().hasFollower(BigFollower.class)) {
            actions.add(new MeepleAction(BigFollower.class, sites));
        }
        if (getActivePlayer().hasFollower(Phantom.class)) {
            actions.add(new MeepleAction(Phantom.class, sites));
        }
        if (cornType.equals(City.class) && getActivePlayer().hasFollower(Mayor.class)) {
            actions.add(new MeepleAction(Mayor.class, sites));
        }
        if (!cornType.equals(Farm.class) && getActivePlayer().hasFollower(Wagon.class)) {
            actions.add(new MeepleAction(Wagon.class, sites));
        }
        return actions;
    }

    private List<PlayerAction> prepareRemovalAction(Class<? extends Feature> cornType) {
        SelectFeatureAction action = null;
        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m instanceof Follower)) continue;
            if (m.getPlayer() != getActivePlayer()) continue;
            if (!cornType.isInstance(m.getFeature())) continue;
            if (action == null) {
                action = new UndeployAction("undeploy", PlayerRestriction.only(getActivePlayer()));
            }
            action.getOrCreate(m.getPosition()).add(m.getLocation());
        }
        if (action == null) return Collections.emptyList();
        return Collections.<PlayerAction>singletonList(action);
    }

    @Override
    public void undeployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        if (cornCircleCap.getCornCircleOption() != CornCicleOption.REMOVAL) {
            logger.error("Removal not selected as corn options.");
            return;
        }
        Meeple m = game.getMeeple(p, loc, meepleType, game.getPlayer(meepleOwner));
        Class<? extends Feature> cornType = getTile().getCornCircle();
        if (!cornType.isInstance(m.getFeature())) {
            logger.error("Improper feature type");
            return;
        }
        m.undeploy();
        nextCornPlayer();
    }

    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        if (cornCircleCap.getCornCircleOption() != CornCicleOption.DEPLOYMENT) {
            logger.error("Deployment not selected as corn options.");
            return;
        }
        List<Meeple> meeples = getBoard().get(p).getFeature(loc).getMeeples();
        if (meeples.isEmpty()) {
            logger.error("Feature must be occupies");
            return;
        }
        if (meeples.get(0).getPlayer() != getActivePlayer()) {
            logger.error("Feature must be occupies with own follower");
            return;
        }

        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        Tile tile = getBoard().get(p);
        m.deployUnchecked(tile, loc, tile.getFeature(loc));
        game.fireGameEvent().deployed(m);
        nextCornPlayer();
    }

    @Override
    public void pass() {
        if (cornCircleCap.getCornCircleOption() == CornCicleOption.REMOVAL) {
            logger.error("Removal cannot be passed");
            return;
        }
        nextCornPlayer();
    }

    @Override
    public void loadGame(Snapshot snapshot) {
        setEntered(true); //avoid call enter on load phase to this phase switch
        if (cornCircleCap.getCornCircleOption() == null) {
            game.getUserInterface().selectCornCircleOption();
        } else {
            prepareCornAction();
        }
    }
}
