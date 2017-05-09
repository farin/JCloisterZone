package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Quarter;
import com.jcloisterzone.feature.visitor.IsCompleted;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;

@RequiredCapability(CountCapability.class)
public class CocPreScorePhase extends Phase {

    public CocPreScorePhase(GameController gc) {
        super(gc);
    }

    @Override
    public Player getActivePlayer() {
        Player p = countCap.getMoveOutPlayer();
        return p == null ? game.getTurnPlayer() : p;
    }

    private List<PlayerAction<?>> preparePlayerActions(Player player) {
        //List<PlayerAction<?>> result = new ArrayList<>();
        Map<Class<? extends Follower>, MeepleAction> actions = new HashMap<>();
        for (Feature feature : getTile().getFeatureMap()) {
            if (feature instanceof Farm) {
                continue;
            }
            if (feature.walk(new IsCompleted())) {
                Quarter q = countCap.getQuarterFor(feature);
                if (countCap.getCount().getLocation() == q.getLocation()) {
                    continue;
                }
                for (Meeple m : q.getMeeples()) {
                    Follower f = (Follower) m;
                    if (f.getPlayer() != player) {
                        continue;
                    }
                    MeepleAction action = actions.get(f.getClass());
                    if (action == null) {
                        action = new MeepleAction(f.getClass());
                        actions.put(f.getClass(), action);
                    }
                    action.add(new FeaturePointer(feature));
                }
            }
        }
        return new ArrayList<>(actions.values());
    }

    @Override
    public void next() {
        countCap.setMoveOutPlayer(null);
        super.next();
    }

    private Player nextPlayer() {
        Player player = countCap.getMoveOutPlayer();
        if (player == game.getTurnPlayer()) {
            return null;
        }
        //check for null after turn player check (important for 1 player game)
        if (player == null) {
            player = game.getNextPlayer();
        } else {
            player = game.getNextPlayer(player);
        }
        countCap.setMoveOutPlayer(player);
        return player;
    }

    @Override
    public void enter() {
        Player player = nextPlayer();
        while (true) {
            List<PlayerAction<?>> actions = preparePlayerActions(player);
            if (actions.isEmpty()) {
                if ((player = nextPlayer()) == null) {
                    next();
                    return;
                }
            } else {
                toggleClock(player);
                game.post(new SelectActionEvent(player, actions, true));
                return;
            }
        }
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        Player player = nextPlayer();
        if (player == null) {
            next();
        } else {
            enter();
        }
    }

    @WsSubscribe
    public void handleDeployMeeple(DeployMeepleMessage msg) {
        assert getTile().getPosition().equals(fp.getPosition());
        Player player = countCap.getMoveOutPlayer();
        Feature f = getBoard().getPlayer(fp);
        Quarter quarter = countCap.getQuarterFor(f);
        for (Meeple m : quarter.getMeeples()) {
            if (m.getPlayer() == player && meepleType.isInstance(m)) {
                quarter.removeMeeple(m);
                m.deploy(fp);
                enter();
                return;
            }
        }
        throw new IllegalArgumentException("Mepple doesn't exist");
    }
}
