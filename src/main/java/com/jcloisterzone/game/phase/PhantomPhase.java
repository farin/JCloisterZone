package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.PlaceTunnel;
import io.vavr.Predicates;
import io.vavr.collection.Vector;

public class PhantomPhase extends AbstractActionPhase {

    public PhantomPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        if (state.getFlags().contains(Flag.NO_PHANTOM)) {
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

        TunnelCapability tunnelCapability = state.getCapabilities().get(TunnelCapability.class);
        if (tunnelCapability != null) {
            state = tunnelCapability.onActionPhaseEntered(state);
        }

        if (actions.isEmpty()) {
            return next(state);
        } else {
            return promote(state);
        }
    }

    @PhaseMessageHandler
    public StepResult handlePlaceToken(GameState state, PlaceTokenMessage msg) {
        Player player = state.getActivePlayer();
        Token token = msg.getToken();

        state = state.mapPlayers(ps ->
                ps.addTokenCount(player.getIndex(), token, -1)
        );

        if (!(token instanceof TunnelCapability.Tunnel)) {
            throw new IllegalArgumentException("Only tunnel token placement is allowed");
        }

        FeaturePointer ptr = (FeaturePointer) msg.getPointer();
        state = (new PlaceTunnel((TunnelCapability.Tunnel) token, ptr)).apply(state);
        state = clearActions(state);
        return enter(state);
    }
}
