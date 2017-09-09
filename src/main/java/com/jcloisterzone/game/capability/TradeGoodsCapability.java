package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeGoods;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoringResult;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class TradeGoodsCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoringResult> completed) {
        for (Feature feature : completed.keySet()) {
            if (!(feature instanceof City)) continue;

            City city = (City) feature;
            Map<TradeGoods, Integer> cityTradeGoods = city.getTradeGoods();
            if (cityTradeGoods.isEmpty()) {
                continue;
            }

            int playerIdx = state.getPlayers().getTurnPlayerIndex();
            state = state.mapPlayers(ps -> {
                for (Tuple2<TradeGoods, Integer> t : cityTradeGoods) {
                    ps = ps.addTokenCount(playerIdx, t._1.getToken(), t._2);
                }
                return ps;
            });
        }

        return state;
    }

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && xml.hasAttribute("resource")) {
            City city = (City) feature;
            String val = xml.getAttribute("resource");
            TradeGoods res = TradeGoods.valueOf(val.toUpperCase());
            return city.setTradeGoods(HashMap.of(res, 1));
        }
        return feature;
    }


    @Override
    public GameState onFinalScoring(GameState state) {
        PlayersState ps = state.getPlayers();
        for (TradeGoods tr : TradeGoods.values()) {
            int hiVal = 1;
            List<Player> hiPlayers = List.empty();
            Token token = tr.getToken();

            for (Player player: ps.getPlayers()) {
                int playerValue = ps.getPlayerTokenCount(player.getIndex(), token);
                if (playerValue > hiVal) {
                    hiVal = playerValue;
                    hiPlayers = List.of(player);
                } else if (playerValue == hiVal) {
                    hiPlayers.prepend(player);
                }
            }
            for (Player player: hiPlayers) {
                state = (new AddPoints(player, 10, PointCategory.TRADE_GOODS)).apply(state);
            }
        }
        return state;
    }
}
