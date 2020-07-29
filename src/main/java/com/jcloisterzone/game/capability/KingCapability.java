package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenReceivedEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.BiggestFeatureAward;
import com.jcloisterzone.game.Capability;
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
        City biggestCityCompleted = null;

        for (Scoreable feature : completed.keySet()) {
            if (feature instanceof City && feature.getTilePositions().size() > maxCitySize) {
                biggestCityCompleted = (City) feature;
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

        for (Player player: ps.getPlayers()) {
            if (ps.getPlayerTokenCount(player.getIndex(), BiggestFeatureAward.KING) > 0) {
                state = (new AddPoints(player, countCompletedCities(state))).apply(state);
            }
        }
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

