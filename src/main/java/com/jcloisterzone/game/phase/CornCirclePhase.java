package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.CornCircleSelectOptionEvent;
import com.jcloisterzone.event.CornCirclesOptionEvent;
import com.jcloisterzone.event.SelectActionEvent;
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
import com.jcloisterzone.ui.GameController;

public class CornCirclePhase extends ServerAwarePhase {

    private final CornCircleCapability cornCircleCap;

    public CornCirclePhase(Game game, GameController controller) {
        super(game, controller);
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
        Class<? extends Feature> cornType = getTile().getCornCircle();
        if (cornType == null) {
            next();
            return;
        }
        boolean deployedFollowerExists = false;
        for (Meeple m : game.getDeployedMeeples()) {
            if (m instanceof Follower && cornType.isInstance(m.getFeature())) {
                deployedFollowerExists = true;
                break;
            }
        }
        if (!deployedFollowerExists) {
            next();
            return;
        }
        game.post(new CornCircleSelectOptionEvent(getActivePlayer(), getTile().getPosition()));
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
        game.post(new CornCirclesOptionEvent(getActivePlayer(), cornCircleCap.getCornCircleOption()));
        Player cornPlayer = game.getNextPlayer(getActivePlayer());
        cornCircleCap.setCornCirclePlayer(cornPlayer);
        prepareCornAction();
    }

    private void prepareCornAction() {
        List<PlayerAction<?>> actions;
        Class<? extends Feature> cornType = getTile().getCornCircle();
        if (cornCircleCap.getCornCircleOption() == CornCicleOption.REMOVAL) {
            actions = prepareRemovalAction(cornType);
        } else {
            actions = prepareDeploymentAction(cornType);
        }
        if (actions.isEmpty()) {
            nextCornPlayer();
        } else {
            boolean passAllowed = cornCircleCap.getCornCircleOption() == CornCicleOption.DEPLOYMENT;
            Player activePlayer = getActivePlayer();
            toggleClock(activePlayer);
            game.post(new SelectActionEvent(activePlayer, actions, passAllowed));
        }
    }

    private List<PlayerAction<?>> prepareDeploymentAction(Class<? extends Feature> cornType) {
        List<FeaturePointer> pointers = new ArrayList<>();
        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m instanceof Follower)) continue;
            if (m.getPlayer() != getActivePlayer()) continue;
            if (!cornType.isInstance(m.getFeature())) continue;
            pointers.add(new FeaturePointer(m.getPosition(), m.getLocation()));
        }
        if (pointers.isEmpty()) return Collections.emptyList();

        List<PlayerAction<?>> actions = new ArrayList<>();
        //TODO nice to do this in generic way independently on particular followers enumeration
        if (getActivePlayer().hasFollower(SmallFollower.class)) {
            actions.add(new MeepleAction(SmallFollower.class).addAll(pointers));
        }
        if (getActivePlayer().hasFollower(BigFollower.class)) {
            actions.add(new MeepleAction(BigFollower.class).addAll(pointers));
        }
        if (getActivePlayer().hasFollower(Phantom.class)) {
            actions.add(new MeepleAction(Phantom.class).addAll(pointers));
        }
        if (cornType.equals(City.class) && getActivePlayer().hasFollower(Mayor.class)) {
            actions.add(new MeepleAction(Mayor.class).addAll(pointers));
        }
        if (!cornType.equals(Farm.class) && getActivePlayer().hasFollower(Wagon.class)) {
            actions.add(new MeepleAction(Wagon.class).addAll(pointers));
        }
        return actions;
    }

    private List<PlayerAction<?>> prepareRemovalAction(Class<? extends Feature> cornType) {
        UndeployAction action = null;
        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m instanceof Follower)) continue;
            if (m.getPlayer() != getActivePlayer()) continue;
            if (!cornType.isInstance(m.getFeature())) continue;
            if (action == null) {
                action = new UndeployAction("undeploy");
            }
            action.add(new MeeplePointer(m));
        }
        if (action == null) return Collections.emptyList();
        return Collections.<PlayerAction<?>>singletonList(action);
    }

    @Override
    public void undeployMeeple(MeeplePointer mp) {
        if (cornCircleCap.getCornCircleOption() != CornCicleOption.REMOVAL) {
            logger.error("Removal not selected as corn options.");
            return;
        }
        Meeple m = game.getMeeple(mp);
        Class<? extends Feature> cornType = getTile().getCornCircle();
        if (!cornType.isInstance(m.getFeature())) {
            logger.error("Improper feature type");
            return;
        }
        m.undeploy();
        nextCornPlayer();
    }

    @Override
    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
        if (cornCircleCap.getCornCircleOption() != CornCicleOption.DEPLOYMENT) {
            logger.error("Deployment wasn't selected as corn options.");
            return;
        }
        List<Meeple> meeples = getBoard().get(fp).getMeeples();
        if (meeples.isEmpty()) {
            logger.error("Feature must be occupied");
            return;
        }
        if (meeples.get(0).getPlayer() != getActivePlayer()) {
            logger.error("Feature must be occupies with own follower");
            return;
        }

        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        m.deploy(fp);
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
            game.post(new CornCircleSelectOptionEvent(game.getActivePlayer(), getTile().getPosition()));
        } else {
            prepareCornAction();
        }
    }
}
