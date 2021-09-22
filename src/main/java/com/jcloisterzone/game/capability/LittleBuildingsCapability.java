package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.LittleBuildingAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.game.state.mixins.RulesMixin;
import io.vavr.collection.*;

import java.util.Arrays;

/**
 * Model is map of all placed little buildings.
 */
public class LittleBuildingsCapability extends Capability<Map<Position, LittleBuilding>> {

	public enum LittleBuilding implements Token {
		LB_SHED,
		LB_HOUSE,
		LB_TOWER
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

    public static List<ExprItem> getBuildingsPoints(RulesMixin rules, Seq<LittleBuilding> buildings) {
        List<ExprItem> result = List.empty();
        if ("3/2/1".equals(rules.getStringRule(Rule.LITTLE_BUILDINGS_SCORING))) {
            Map<LittleBuilding, Integer> counts = buildings.groupBy(t -> t).mapValues(l -> l.size());
            int shedCount = counts.getOrElse(LittleBuilding.LB_SHED, 0);
            if (shedCount > 0) {
                result = result.append(new ExprItem(shedCount, "little-buildings." + LittleBuilding.LB_SHED.name(), 1 * shedCount));
            }
            int houseCount = counts.getOrElse(LittleBuilding.LB_HOUSE, 0);
            if (houseCount > 0) {
                result = result.append(new ExprItem(houseCount, "little-buildings." + LittleBuilding.LB_HOUSE.name(), 2 * houseCount));
            }
            int towerCount = counts.getOrElse(LittleBuilding.LB_TOWER, 0);
            if (towerCount > 0) {
                result = result.append(new ExprItem(towerCount, "little-buildings." + LittleBuilding.LB_TOWER.name(), 3 * towerCount));
            }
        } else {
            int count = buildings.size();
            if (count > 0) {
                result = result.append(new ExprItem(count, "little-buildings", count));
            }
        }
        return result;
    }
}
