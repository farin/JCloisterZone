package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FlierCapability;
import com.jcloisterzone.game.capability.GermanMonasteriesCapability;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.DeployFlierMessage;

public class PhantomPhase extends Phase {

    private final TowerCapability towerCap;
    private final FlierCapability flierCap;
    private final PrincessCapability princessCap;
    private final GermanMonasteriesCapability gmCap;

    public PhantomPhase(Game game) {
        super(game);
        towerCap = game.getCapability(TowerCapability.class);
        flierCap = game.getCapability(FlierCapability.class);
        princessCap = game.getCapability(PrincessCapability.class);
        gmCap = game.getCapability(GermanMonasteriesCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(PhantomCapability.class);
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void enter() {
        //TODO what about pay ransom for phantom now?
        if (!getActivePlayer().hasFollower(Phantom.class) || (princessCap != null && princessCap.isPrincessUsed())) {
            next();
            return;
        }
        MeepleAction phantomAction = new MeepleAction(Phantom.class);
        List actions = Collections.singletonList(phantomAction);
        Set<FeaturePointer> followerLocations = game.prepareFollowerLocations();
        phantomAction.addAll(followerLocations);

        //hardcoded - byt need to pass false to prepareFlier
        if (towerCap != null) {
            towerCap.prepareTowerFollowerDeploy(actions);
        }
        if (flierCap != null) {
            flierCap.prepareFlier(actions, false);
        }
        if (gmCap != null) { //it should be call on all capabilities, but now flier is call separatelly with different arg
            gmCap.postPrepareActions(actions);
        }

        if (phantomAction.isEmpty()) {
            next();
        } else {
            game.post(new SelectActionEvent(getActivePlayer(), actions, true));
        }
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

    @WsSubscribe
    public void handleDeployFlier(DeployFlierMessage msg) {
        game.getPhases().getInstance(ActionPhase.class).handleDeployFlier(msg);
    }

}
