package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Predicates;
import io.vavr.collection.Vector;

@RequiredCapability(PhantomCapability.class)
public class PhantomPhase extends AbstractActionPhase {

    public PhantomPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        if (state.getFlags().contains(Flag.PRINCESS_USED)) {
            // The placement of a princess tile with removal of a knight from the city cannot be used as a first
            // "follower move" and be followed by placement of the phantom (e.g. into the now-vacated city).
            // As per the rules for the princess, "if a knight is removed from the city, the player may not deploy or
            // move any other figure." [This combo would be too powerful in allowing city stealing – ed.]
            return next(state);
        }

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
