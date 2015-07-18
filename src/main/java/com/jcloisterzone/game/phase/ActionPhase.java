package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.FlierRollEvent;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.FlierCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.DeployFlierMessage;


public class ActionPhase extends Phase {

    private final TowerCapability towerCap;
    private final FlierCapability flierCap;
    private final PortalCapability portalCap;
    private final PrincessCapability princessCapability;

    public ActionPhase(Game game) {
        super(game);
        towerCap = game.getCapability(TowerCapability.class);
        flierCap = game.getCapability(FlierCapability.class);
        portalCap = game.getCapability(PortalCapability.class);
        princessCapability = game.getCapability(PrincessCapability.class);
    }

    @Override
    public void enter() {
        List<PlayerAction<?>> actions = new ArrayList<>();

        Set<FeaturePointer> followerLocations = game.prepareFollowerLocations();
        if (getActivePlayer().hasFollower(SmallFollower.class)  && !followerLocations.isEmpty()) {
            actions.add(new MeepleAction(SmallFollower.class).addAll(followerLocations));
        }
        game.prepareActions(actions, ImmutableSet.copyOf(followerLocations));
        game.post(new SelectActionEvent(getActivePlayer(), actions, true));
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
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

    @Override
    public void placeTowerPiece(Position p) {
        towerCap.placeTowerPiece(getActivePlayer(), p);
        next(TowerCapturePhase.class);
    }

    @Override
    public void placeLittleBuilding(LittleBuilding lbType) {
        LittleBuildingsCapability lbCap = game.getCapability(LittleBuildingsCapability.class);
        lbCap.placeLittleBuilding(getActivePlayer(), lbType);
        next();
    }

    @Override
    public void moveNeutralFigure(BoardPointer ptr, Class<? extends NeutralFigure> figureType) {
        if (Fairy.class.equals(figureType)) {
            if (!Iterables.any(getActivePlayer().getFollowers(), MeeplePredicates.at(ptr.getPosition()))) {
                throw new IllegalArgumentException("The tile has deployed not own follower.");
            }
            Fairy fairy = game.getCapability(FairyCapability.class).getFairy();
            if (game.getBooleanValue(CustomRule.FAIRY_ON_TILE)) {
                fairy.deploy(ptr.getPosition());
            } else {
                fairy.deploy((MeeplePointer) ptr);
            }
            next();
        } else {
            super.moveNeutralFigure(ptr, figureType);
        }
    }

    private boolean isFestivalUndeploy(Meeple m) {
        return getTile().hasTrigger(TileTrigger.FESTIVAL) && m.getPlayer() == getActivePlayer();
    }

    private boolean isPrincessUndeploy(Meeple m) {
        boolean tileHasPrincess = false;
        for (Feature f : getTile().getFeatures()) {
            if (f instanceof City) {
                City c = (City) f;
                if (c.isPricenss()) {
                    tileHasPrincess = true;
                    break;
                }
            }
        }
        //check if it is same city should be here to be make exact check
        return tileHasPrincess && m.getFeature() instanceof City;
    }

    @Override
    public void undeployMeeple(MeeplePointer mp) {
        Meeple m = game.getMeeple(mp);
        boolean princess = isPrincessUndeploy(m);
        if (isFestivalUndeploy(m) || princess) {
            m.undeploy();
            if (princess) {
                princessCapability.setPrincessUsed(true);
            }
            next();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void placeTunnelPiece(FeaturePointer fp, boolean isB) {
        game.getCapability(TunnelCapability.class).placeTunnelPiece(fp, isB);
        next(ActionPhase.class);
    }


    @Override
    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        //TODO nice to have validation in separate class (can be turned off eg for loadFromSnapshots or in AI (to speed it)
        if (m instanceof Follower) {
            if (getBoard().get(fp).walk(new IsOccupied())) {
                throw new IllegalArgumentException("Feature is occupied.");
            }
        }
        m.deploy(fp);
        if (portalCap != null && fp.getLocation() != Location.TOWER && getTile().hasTrigger(TileTrigger.PORTAL) && !fp.getPosition().equals(getTile().getPosition())) {
            //magic gate usage
            portalCap.setPortalUsed(true);
        }
        next();
    }

    @Override
    public void deployBridge(Position pos, Location loc) {
        BridgeCapability bridgeCap = game.getCapability(BridgeCapability.class);
        bridgeCap.decreaseBridges(getActivePlayer());
        bridgeCap.deployBridge(pos, loc);
        next(ActionPhase.class);
    }

    @WsSubscribe
    public void handleDeployFlier(DeployFlierMessage msg) {
        game.updateRandomSeed(msg.getCurrentTime());
        int distance = game.getRandom().nextInt(3) + 1;
        flierCap.setFlierUsed(true);
        flierCap.setFlierDistance(msg.getMeepleTypeClass(), distance);
        game.post(new FlierRollEvent(getActivePlayer(), getTile().getPosition(), distance));
        next(FlierActionPhase.class);
    }
}
