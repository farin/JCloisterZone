package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTunnelToken;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

/**
 * Capability model is {@code Map<FeaturePointer, String>} - tunnels,
 *   key is tunnel token id or null if nothing is placed
 */
public final class TunnelCapability extends Capability<Map<FeaturePointer, PlacedTunnelToken>> {

	public static enum Tunnel implements Token {
		TUNNEL_A,
		TUNNEL_B,
		TUNNEL_C;
	}

	private static final long serialVersionUID = 1L;

    @Override
    public GameState onStartGame(GameState state) {
        int playersCount = state.getPlayers().getPlayers().length();
        String moreTokens = state.getStringRule(Rule.MORE_TUNNEL_TOKENS);
        state = state.mapPlayers(ps -> {
            ps = ps.setTokenCountForAllPlayers(Tunnel.TUNNEL_A, 2);
            if (playersCount == 3 && "3/2".equals(moreTokens)) {
                ps = ps.setTokenCountForAllPlayers(Tunnel.TUNNEL_B, 2);
            }
            if (playersCount < 3) {
                if (!"1/1".equals(moreTokens)) {
                    ps = ps.setTokenCountForAllPlayers(Tunnel.TUNNEL_B, 2);
                    if ("3/2".equals(moreTokens)) {
                        ps = ps.setTokenCountForAllPlayers(Tunnel.TUNNEL_C, 2);
                    }
                }
            }
            return ps;
        });
        state = setModel(state, HashMap.empty());
        return state;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getPlayerActions().getPlayer();

        if (state.hasFlag(Flag.TUNNEL_PLACED)) {
            return state;
        }

        Set<FeaturePointer> openTunnels = getModel(state)
            .filterValues(Predicates.isNull())
            .map(Tuple2::_1)
            .toSet();

        if (openTunnels.isEmpty()) {
            return state;
        }

        ActionsState as = state.getPlayerActions();
        for (Tunnel token : Tunnel.values()) {
            if (state.getPlayers().getPlayerTokenCount(player.getIndex(), token) == 0) {
                continue;
            }

            as = as.appendAction(new TunnelAction(openTunnels, token));
        }

        return state.setPlayerActions(as);
    }
}
