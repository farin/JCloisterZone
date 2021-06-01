package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.event.TokenReceivedEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.setup.GameElementQuery;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.AddPoints;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.w3c.dom.Element;

public class TradeGoodsCapability extends Capability<Void> {
    private static final long serialVersionUID = 1L;

	public enum TradeGoods implements Token {
	    WINE,
	    CLOTH,
	    GRAIN
    }

    public static TradeGoodsModifier TRADE_GOODS = new TradeGoodsModifier();

    private static final int RESOURCE_POINTS = 10;

    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
        for (Feature feature : completed.keySet()) {
            if (!(feature instanceof City)) continue;

            City city = (City) feature;
            Map<TradeGoods, Integer> cityTradeGoods = city.getModifier(state, TRADE_GOODS, null);
            if (cityTradeGoods == null) {
                continue;
            }

            int playerIdx = state.getPlayers().getTurnPlayerIndex();
            state = state.mapPlayers(ps -> {
                for (Tuple2<TradeGoods, Integer> t : cityTradeGoods) {
                    ps = ps.addTokenCount(playerIdx, t._1, t._2);
                }
                return ps;
            });
            for (Tuple2<TradeGoods, Integer> t : cityTradeGoods) {
                TokenReceivedEvent ev = new TokenReceivedEvent(
                    PlayEventMeta.createWithActivePlayer(state),
                    state.getPlayers().getTurnPlayer(),
                    t._1, t._2
                );
                ev.setSourceFeature(feature);
                state = state.appendEvent(ev);
            }
        }

        return state;
    }

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && xml.hasAttribute("resource")) {
            City city = (City) feature;
            String val = xml.getAttribute("resource");
            TradeGoods res = TradeGoods.valueOf(val.toUpperCase());
            feature = city.putModifier(TRADE_GOODS, HashMap.of(res, 1));
        }
        return feature;
    }


    @Override
    public GameState onFinalScoring(GameState state) {
        PlayersState ps = state.getPlayers();
        for (TradeGoods tr : TradeGoods.values()) {
            int hiVal = 1;
            List<Player> hiPlayers = List.empty();

            for (Player player: ps.getPlayers()) {
                int playerValue = ps.getPlayerTokenCount(player.getIndex(), tr);
                if (playerValue > hiVal) {
                    hiVal = playerValue;
                    hiPlayers = List.of(player);
                } else if (playerValue == hiVal) {
                    hiPlayers = hiPlayers.prepend(player);
                }
            }
            List<ReceivedPoints> pts = List.empty();
            for (Player player: hiPlayers) {
                state = (new AddPoints(player, RESOURCE_POINTS)).apply(state);
                PointsExpression expr = new PointsExpression("trade-goods", new ExprItem("trade-goods." + tr.name(), RESOURCE_POINTS));
                pts = pts.append(new ReceivedPoints(expr, player, null));
            }
            if (!pts.isEmpty()) {
                state = state.appendEvent(new ScoreEvent(pts, false, true));
            }
        }
        return state;
    }

    public static class TradeGoodsModifier extends FeatureModifier<Map<TradeGoods, Integer>> {

        public TradeGoodsModifier() {
            super("city[resource]", new GameElementQuery("trade-goods"));
        }

        @Override
        public Map<TradeGoods, Integer> mergeValues(Map<TradeGoods, Integer> tg1, Map<TradeGoods, Integer> tg2) {
            return tg1.merge(tg2, (a, b) -> a + b);
        }

        @Override
        public Map<TradeGoods, Integer> valueOf(String attr) {
            throw new UnsupportedOperationException();
        }
    }
}
