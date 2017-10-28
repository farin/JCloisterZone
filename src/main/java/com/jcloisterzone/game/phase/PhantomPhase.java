package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.collection.Vector;

@RequiredCapability(PhantomCapability.class)
public class PhantomPhase extends AbstractActionPhase {

    public PhantomPhase(Random random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();

        Vector<PlayerAction<?>> actions = prepareMeepleActions(state, Vector.of(Phantom.class));

        state = state.setPlayerActions(
            new ActionsState(player, actions, true)
        );

        TowerCapability towerCap = state.getCapabilities().get(TowerCapability.class);
        if (towerCap != null) {
            // Phantom can be placed on top of towers
            // use onActionPhaseEntered and filter just Phantom action
            state = towerCap.onActionPhaseEntered(state);
            state = state.mapPlayerActions(as -> as.setActions(
                as.getActions()
                    .filter(Predicates.instanceOf(MeepleAction.class))
                    .filter(a -> ((MeepleAction) a).getMeepleType().equals(Phantom.class))
            ));
        }

        if (actions.isEmpty()) {
            return next(state);
        } else {
            return promote(state);
        }
    }
}
