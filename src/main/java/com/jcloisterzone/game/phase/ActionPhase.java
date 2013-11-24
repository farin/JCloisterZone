package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;
import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TakePrisonerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.FlierCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;


public class ActionPhase extends Phase {

    private final TowerCapability towerCap;
    private final FlierCapability flierCap;

    public ActionPhase(Game game) {
        super(game);
        towerCap = game.getCapability(TowerCapability.class);
        flierCap = game.getCapability(FlierCapability.class);
    }

    @Override
    public void enter() {
        List<PlayerAction> actions = new ArrayList<>();

        LocationsMap locMap = game.prepareFollowerLocations();
        if (getActivePlayer().hasFollower(SmallFollower.class)  && !locMap.isEmpty()) {
            actions.add(new MeepleAction(SmallFollower.class, locMap));
        }
        game.prepareActions(actions, locMap);
        if (isAutoTurnEnd(actions)) {
            next();
        } else {
            notifyUI(actions, true);
        }
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    private boolean isAutoTurnEnd(List<PlayerAction> actions) {
        if (!actions.isEmpty()) return false;
        if (towerCap != null && !towerCap.isRansomPaidThisTurn() && towerCap.hasImprisonedFollower(getActivePlayer())) {
            //player can return figure immediately
            return false;
        }
        if (flierCap != null && flierCap.isFlierRollAllowed()) {
            return false;
        }
        return true;
    }

    @Override
    public void pass() {
        if (getDefaultNext() instanceof PhantomPhase) {
            //skip PhantomPhase if user pass turn
            getDefaultNext().next();
        } else {
            next();
        }
    }

    private int doPlaceTowerPiece(Position p) {
        Tower tower = getBoard().get(p).getTower();
        if (tower  == null) {
            throw new IllegalArgumentException("No tower on tile.");
        }
        if (tower.getMeeple() != null) {
            throw new IllegalArgumentException("The tower is sealed");
        }
        towerCap.decreaseTowerPieces(getActivePlayer());
        return tower.increaseHeight();
    }

    public TakePrisonerAction prepareCapture(Position p, int range) {
        //TODO custom rule - opponent only
        TakePrisonerAction captureAction = new TakePrisonerAction(PlayerRestriction.any());
        for (Meeple pf : game.getDeployedMeeples()) {
            if (!(pf instanceof Follower)) continue;
            Position pos = pf.getPosition();
            if (pos.x != p.x && pos.y != p.y) continue; //check if is in same row or column
            if (pos.squareDistance(p) > range) continue;
            captureAction.getOrCreate(pos).add(pf.getLocation());
        }
        return captureAction;
    }

    @Override
    public void placeTowerPiece(Position p) {
        int captureRange = doPlaceTowerPiece(p);
        game.fireGameEvent().towerIncreased(p, captureRange);
        TakePrisonerAction captureAction = prepareCapture(p, captureRange);
        if (captureAction.getLocationsMap().isEmpty()) {
            next();
            return;
        }
        next(TowerCapturePhase.class);
        notifyUI(captureAction, false);
    }

    @Override
    public void moveFairy(Position p) {
        if (!Iterables.any(getActivePlayer().getFollowers(), MeeplePredicates.at(p))) {
            throw new IllegalArgumentException("The tile has deployed not own follower.");
        }

        game.getCapability(FairyCapability.class).setFairyPosition(p);
        game.fireGameEvent().fairyMoved(p);
        next();
    }

    private boolean isFestivalUndeploy(Meeple m) {
        return getTile().hasTrigger(TileTrigger.FESTIVAL) && m.getPlayer() == getActivePlayer();
    }

    private boolean isPrincessUndeploy(Meeple m) {
        //TODO proper validation
        return m.getFeature() instanceof City;
    }

    @Override
    public void undeployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        Meeple m = game.getMeeple(p, loc, meepleType, game.getPlayer(meepleOwner));
        if (isFestivalUndeploy(m) || isPrincessUndeploy(m)) {
            m.undeploy();
            next();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void placeTunnelPiece(Position p, Location loc, boolean isB) {
        game.getCapability(TunnelCapability.class).placeTunnelPiece(p, loc, isB);
        next(ActionPhase.class);
    }


    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        m.deploy(getBoard().get(p), loc);
        next();
    }

    @Override
    public void deployBridge(Position pos, Location loc) {
        BridgeCapability bridgeCap = game.getCapability(BridgeCapability.class);
        bridgeCap.decreaseBridges(getActivePlayer());
        bridgeCap.deployBridge(pos, loc);
        next(ActionPhase.class);
    }

    @Override
    public void setFlierDistance(int distance) {
        flierCap.setFlierDistance(distance);
        next(FlierActionPhase.class);
    }

}
