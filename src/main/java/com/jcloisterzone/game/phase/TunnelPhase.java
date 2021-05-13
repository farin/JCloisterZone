package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.reducers.PlaceTunnel;
import io.vavr.collection.Vector;

public class TunnelPhase extends Phase {

    public TunnelPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        if (state.hasFlag(Flag.TUNNEL_PLACED)) {
            return next(state);
        }

        TunnelCapability cap = state.getCapabilities().get(TunnelCapability.class);
        java.util.List<TunnelAction> actions = cap.createTunnelActions(state);
        if (actions.isEmpty()) {
            return next(state);
        }

        return promote(state.setPlayerActions(new ActionsState(state.getTurnPlayer(), Vector.ofAll(actions), true)));
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
