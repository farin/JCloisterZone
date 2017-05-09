package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;

@RequiredCapability(CountCapability.class)
public class CocFollowerPhase extends Phase {

    public CocFollowerPhase(GameController gc) {
        super(gc);
    }

    @Override
    public void enter() {
        Player activePlayer = game.getActivePlayer();
        boolean canMove = false;
        for (Player p : game.getAllPlayers()) {
            if (activePlayer == p) {
                if (countCap.didReceivePoints(p)) {
                    canMove = false;
                    break;
                }
            } else {
                canMove = canMove || countCap.didReceivePoints(p);
            }
        }
        if (!canMove) {
            next();
            return;
        }

        Position pos = countCap.getQuarterPosition();

        List<PlayerAction<?>> actions = new ArrayList<>();
        Set<FeaturePointer> fullSet = new ImmutableSet.Builder<FeaturePointer>()
            .add(new FeaturePointer(pos, Location.QUARTER_CASTLE))
            .add(new FeaturePointer(pos, Location.QUARTER_MARKET))
            .add(new FeaturePointer(pos, Location.QUARTER_BLACKSMITH))
            .add(new FeaturePointer(pos, Location.QUARTER_CATHEDRAL))
            .build();

        if (activePlayer.hasFollower(SmallFollower.class)) {
            actions.add(new MeepleAction(SmallFollower.class).addAll(fullSet));
        }
        if (activePlayer.hasFollower(BigFollower.class)) {
            actions.add(new MeepleAction(BigFollower.class).addAll(fullSet));
        }
        if (activePlayer.hasFollower(Phantom.class)) {
            actions.add(new MeepleAction(Phantom.class).addAll(fullSet));
        }
        if (activePlayer.hasFollower(Mayor.class)) {
            actions.add(new MeepleAction(Mayor.class).addAll(
                Collections.singleton(new FeaturePointer(pos, Location.QUARTER_CASTLE))
            ));
        }
        if (activePlayer.hasFollower(Wagon.class)) {
            actions.add(new MeepleAction(Mayor.class).addAll(
                new ImmutableSet.Builder<FeaturePointer>()
                .add(new FeaturePointer(pos, Location.QUARTER_CASTLE))
                .add(new FeaturePointer(pos, Location.QUARTER_BLACKSMITH))
                .add(new FeaturePointer(pos, Location.QUARTER_CATHEDRAL))
                .build()
            ));
        }

        if (actions.isEmpty()) {
            next();
        } else {
            game.post(new SelectActionEvent(activePlayer, actions, true));
        }
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        next();
    }

    @WsSubscribe
    public void handleDeployMeeple(DeployMeepleMessage msg) {
        if (!fp.getLocation().isCityOfCarcassonneQuarter()) {
            throw new IllegalArgumentException("Only deplpy to the City of Carcassone is allowed");
        }
        if (!fp.getPosition().equals(countCap.getQuarterPosition())) {
            throw new IllegalArgumentException("Illegal position");
        }
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        m.deploy(fp);
        next(CocCountPhase.class);
    }
}