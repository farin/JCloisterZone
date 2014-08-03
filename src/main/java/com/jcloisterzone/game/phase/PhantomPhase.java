package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.capability.TowerCapability;

public class PhantomPhase extends Phase {

    private final TowerCapability towerCap;

    public PhantomPhase(Game game) {
        super(game);
        towerCap = game.getCapability(TowerCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(PhantomCapability.class);
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    @Override
    public void enter() {
        if (!getActivePlayer().hasFollower(Phantom.class)) {
            next();
        }
        MeepleAction phantomAction = new MeepleAction(Phantom.class);
        List<MeepleAction> actions = Collections.singletonList(phantomAction);
        phantomAction.addAll(game.prepareFollowerLocations());
        Set<FeaturePointer> commonSites = game.prepareFollowerLocations();
        if (!commonSites.isEmpty()) {
            if (towerCap != null) {
                towerCap.prepareTowerFollowerDeploy(actions);
            }
        }

        if (isAutoTurnEnd(actions)) {
            next();
        } else {
            game.post(new SelectActionEvent(getActivePlayer(), actions, true));
        }
    }

    //TODO copy from Action phase -> merge
    private boolean isAutoTurnEnd(List<? extends PlayerAction<?>> actions) {
        if (!actions.isEmpty()) return false;
        if (towerCap != null && !towerCap.isRansomPaidThisTurn() && towerCap.hasImprisonedFollower(getActivePlayer(), Phantom.class)) {
            //player can return phantom figure immediately
            return false;
        }
        return true;
    }

    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        if (!meepleType.equals(Phantom.class)) {
            throw new IllegalArgumentException("Only phantom can be placed as second follower.");
        }
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        m.deployUnoccupied(getBoard().get(p), loc);
        next();
    }

    @Override
    public void pass() {
        next();
    }

}
