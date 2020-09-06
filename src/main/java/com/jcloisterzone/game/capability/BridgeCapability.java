package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.TokenPlacedEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;


/**
 * @model Set<FeaturePointer> : placed bridges
 */
public class BridgeCapability extends Capability<Set<FeaturePointer>> {

	public enum BridgeToken implements Token {
		BRIDGE
    }

	private static final long serialVersionUID = 1L;

    @Override
    public GameState onStartGame(GameState state) {
        int tokens = state.getPlayers().length() < 5 ? 3 : 2;
        state = state.mapPlayers(ps -> ps.setTokenCountForAllPlayers(BridgeToken.BRIDGE, tokens));
        state = setModel(state, HashSet.empty());
        return state;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getPlayerActions().getPlayer();

        boolean playerHasBridge = state.getPlayers().getPlayerTokenCount(
            player.getIndex(), BridgeToken.BRIDGE) > 0;

        if (!playerHasBridge ||
            state.getCurrentTurnPartEvents().find(
               ev -> ev instanceof TokenPlacedEvent && ((TokenPlacedEvent)ev).getToken() == BridgeToken.BRIDGE
            ).isDefined()) {
            return state;
        }

        Position pos = state.getLastPlaced().getPosition();
        Set<FeaturePointer> options = HashSet.empty();

        for (Location bridgeLoc : Location.BRIDGES) {
            FeaturePointer ptr = new FeaturePointer(pos, bridgeLoc);
            if (state.isBridgePlacementAllowed(ptr)) {
                options = options.add(ptr);
            }
        }

        if (options.isEmpty()) {
            return state;
        }

        return state.appendAction(new BridgeAction(options));
    }

}
