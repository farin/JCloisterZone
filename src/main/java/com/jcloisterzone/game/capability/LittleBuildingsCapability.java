package com.jcloisterzone.game.capability;

import java.util.Arrays;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.LittleBuildingAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlayersState;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

/**
 * Model is map of all placed little buildings.
 */
public class LittleBuildingsCapability extends Capability<Map<Position, Token>> {

    @Override
    public GameState onStartGame(GameState state) {
        int playersCount = state.getPlayers().getPlayers().length();
        int tokensCount = 6 / playersCount;
        state = state.mapPlayers(ps -> {
            ps = ps.setTokenCountForAllPlayers(Token.LB_HOUSE, tokensCount);
            ps = ps.setTokenCountForAllPlayers(Token.LB_SHED, tokensCount);
            ps = ps.setTokenCountForAllPlayers(Token.LB_TOWER, tokensCount);
            return ps;
        });
        state = setModel(state, HashMap.empty());
        return state;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getTurnPlayer();
        PlayersState ps = state.getPlayers();
        Set<Token> options = HashSet.ofAll(Arrays.asList(Token.littleBuildingValues()))
            .filter(lb -> ps.getPlayerTokenCount(player.getIndex(), lb) > 0);

        if (options.isEmpty()) {
            return state;
        }

        Position pos = state.getLastPlaced().getPosition();
        return state.appendAction(new LittleBuildingAction(options, pos));
    }
}
