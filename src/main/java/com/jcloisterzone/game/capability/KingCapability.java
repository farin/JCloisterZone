package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.event.TokenReceivedEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.BiggestFeatureAward;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.MemoizedValue;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.AddPoints;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public final class KingCapability extends Capability<Void> {

    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
        Set<Scoreable> completedFeatures = completed.keySet();
        int maxCitySize = getMaxSize(state, City.class, completedFeatures);
        int completedCitiesThisTurn = 0;
        City biggestCityCompleted = null;

        for (Scoreable feature : completed.keySet()) {
            if (feature instanceof City) {
                completedCitiesThisTurn++;
                if (feature.getTilePositions().size() > maxCitySize) {
                    biggestCityCompleted = (City) feature;
                }
            }
        }

        if (state.getStringRule(Rule.KING_AND_ROBBER_SCORING).equals("continuously") && completedCitiesThisTurn > 0) {
            Player currentHolder = state.getPlayers().getPlayerWithToken(BiggestFeatureAward.KING);
            if (currentHolder != null) {
                ReceivedPoints rp = new ReceivedPoints(new PointsExpression("king", new ExprItem(completedCitiesThisTurn, "cities", completedCitiesThisTurn)), currentHolder, null);
                state = (new AddPoints(rp, false)).apply(state);
            }
        }

        Player turnPlayer = state.getTurnPlayer();
        PlayersState ps = state.getPlayers();
        if (biggestCityCompleted != null) {
            for (Player p : ps.getPlayers()) {
                ps = ps.setTokenCount(p.getIndex(), BiggestFeatureAward.KING, p.equals(turnPlayer) ? 1 : 0);
            }
            TokenReceivedEvent ev = new TokenReceivedEvent(
                    PlayEventMeta.createWithActivePlayer(state), turnPlayer, BiggestFeatureAward.KING, 1);
            ev.setSourceFeature(biggestCityCompleted);
            state = state.appendEvent(ev);
        }
        return state.setPlayers(ps);
    }

    @Override
    public GameState onFinalScoring(GameState state) {
        PlayersState ps = state.getPlayers();
        String rule = state.getStringRule(Rule.KING_AND_ROBBER_SCORING);
        if (rule.equals("continuously")) {
            return state;
        }

        Player player = ps.getPlayerWithToken(BiggestFeatureAward.KING);
        if (player == null) {
            return state;
        }

        String exprName = "king";
        String itemName = "king";
        Integer count = null;
        int points;
        if (rule.equals("10/20")) {
            points = 10;
        } else if (rule.equals("15/40")) {
            boolean hasAlsoRobber = ps.getPlayerTokenCount(player.getIndex(), BiggestFeatureAward.ROBBER) > 0;
            if (hasAlsoRobber) {
                exprName = "king+robber";
                itemName = "king+robber";
                points = 40;
            } else {
                points = 15;
            }
        } else {
            itemName = "cities";
            points = countCompletedCities(state);
            count = points;
        }

        ReceivedPoints rp = new ReceivedPoints(new PointsExpression(exprName, new ExprItem(count, itemName, points)), player, null);
        state = (new AddPoints(rp, false, true)).apply(state);
        return state;
    }

    private int getMaxSize(GameState state, Class<? extends Completable> cls, Set<Scoreable> exclude) {
        return state.getFeatures(cls)
            .filter(c -> !exclude.contains(c))
            .filter(c -> c.isCompleted(state))
            .map(c -> c.getTilePositions().size())
            .max()
            .getOrElse(0);
    }

    private MemoizedValue<Integer> _getBiggestCitySize = new MemoizedValue<>(state -> getMaxSize(state, City.class, HashSet.empty()));

    public int getBiggestCitySize(GameState state) {
        return _getBiggestCitySize.apply(state);
    }

    public int countCompletedCities(GameState state) {
        int count = state.getFeatures(City.class)
            .filter(c -> c.isCompleted(state))
            .size();

        if (state.hasCapability(CountCapability.class)) {
            count += 1;
        }

        return count;
    }
}

