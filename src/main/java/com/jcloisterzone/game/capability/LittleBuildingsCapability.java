package com.jcloisterzone.game.capability;

import java.util.Arrays;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.LittleBuildingAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.game.state.mixins.RulesMixin;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;

/**
 * Model is map of all placed little buildings.
 */
public class LittleBuildingsCapability extends Capability<Map<Position, LittleBuilding>> {

	public static enum LittleBuilding implements Token {
		LB_SHED,
		LB_HOUSE,
		LB_TOWER;
	}

    @Override
    public GameState onStartGame(GameState state) {
        int playersCount = state.getPlayers().getPlayers().length();
        int tokensCount = 6 / playersCount;
        state = state.mapPlayers(ps -> {
            ps = ps.setTokenCountForAllPlayers(LittleBuilding.LB_HOUSE, tokensCount);
            ps = ps.setTokenCountForAllPlayers(LittleBuilding.LB_SHED, tokensCount);
            ps = ps.setTokenCountForAllPlayers(LittleBuilding.LB_TOWER, tokensCount);
            return ps;
        });
        state = setModel(state, HashMap.empty());
        return state;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getTurnPlayer();
        PlayersState ps = state.getPlayers();
        Set<LittleBuilding> options = HashSet.ofAll(Arrays.asList(LittleBuilding.values()))
            .filter(lb -> ps.getPlayerTokenCount(player.getIndex(), lb) > 0);

        if (options.isEmpty()) {
            return state;
        }

        Position pos = state.getLastPlaced().getPosition();
        return state.appendAction(new LittleBuildingAction(options, pos));
    }

    public static int getBuildingsPoints(RulesMixin rules, Seq<LittleBuilding> buildings) {
        if (rules.getBooleanValue(Rule.BULDINGS_DIFFERENT_VALUE)) {
            return buildings
                .map(token -> token.ordinal() + 1)
                .sum().intValue();
        } else {
            return buildings.size();
        }
    }
}
