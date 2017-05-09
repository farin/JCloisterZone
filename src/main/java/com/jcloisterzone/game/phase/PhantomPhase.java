package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.DeployFlierMessage;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;

@RequiredCapability(PhantomCapability.class)
public class PhantomPhase extends Phase {

    public PhantomPhase(GameController gc) {
        super(gc);
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

    @WsSubscribe
    public void handleDeployMeeple(DeployMeepleMessage msg) {
        if (!meepleType.equals(Phantom.class)) {
            throw new IllegalArgumentException("Only phantom can be placed as second follower.");
        }
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        if (m instanceof Follower) {
            if (getBoard().getPlayer(fp).walk(new IsOccupied())) {
                throw new IllegalArgumentException("Feature is occupied.");
            }
        }
        m.deploy(fp);
        next();
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        game.clearLastUndoable();
        next();
    }

    @WsSubscribe
    public void handleDeployFlier(DeployFlierMessage msg) {
        game.getPhases().getInstance(ActionPhase.class).handleDeployFlier(msg);
    }

}
