package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.MemoizedValue;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public final class KingAndRobberBaronCapability extends Capability<Void> {

    @Override
    public GameState onCompleted(GameState state, HashMap<Completable, Integer> completed) {
        Set<Completable> completedFeatures = completed.keySet();
        int maxCitySize = getMaxSize(state, City.class, completedFeatures);
        int maxRoadSize = getMaxSize(state, Road.class, completedFeatures);
        boolean biggestCityCompleted = false;
        boolean longestRoadCompleted = false;

        for (Completable c : completed.keySet()) {
            if (!biggestCityCompleted && c instanceof City) {
                biggestCityCompleted = c.getTilePositions().size() > maxCitySize;
            }
            if (!longestRoadCompleted && c instanceof Road) {
                longestRoadCompleted = c.getTilePositions().size() > maxRoadSize;
            }
        }

        Player turnPlayer = state.getTurnPlayer();
        PlayersState ps = state.getPlayers();
        if (biggestCityCompleted) {
            for (Player p : ps.getPlayers()) {
                ps = ps.setTokenCount(p.getIndex(), Token.KING, p.equals(turnPlayer) ? 1 : 0);
            }
        }
        if (longestRoadCompleted) {
            for (Player p : ps.getPlayers()) {
                ps = ps.setTokenCount(p.getIndex(), Token.ROBBER, p.equals(turnPlayer) ? 1 : 0);
            }
        }
        return state.setPlayers(ps);
    }

    @Override
    public GameState finalScoring(GameState state) {
        PlayersState ps = state.getPlayers();

        for (Player player: ps.getPlayers()) {
            if (ps.getPlayerTokenCount(player.getIndex(), Token.KING) > 0) {
                state = (new AddPoints(player, countCompletedCities(state), PointCategory.BIGGEST_CITY)).apply(state);
            }
            if (ps.getPlayerTokenCount(player.getIndex(), Token.ROBBER) > 0) {
                state = (new AddPoints(player, countCompletedRoads(state), PointCategory.LONGEST_ROAD)).apply(state);
            }
        }
        return state;
    }

    private int getMaxSize(GameState state, Class<? extends Completable> cls, Set<Completable> exclude) {
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

    public int countCompletedRoads(GameState state) {
        return state.getFeatures(Road.class)
            .filter(c -> c.isCompleted(state))
            .size();
    }

    private MemoizedValue<Integer> _getLongestRoadSize = new MemoizedValue<>(state -> getMaxSize(state, Road.class, HashSet.empty()));

    public int getLongestRoadSize(GameState state) {
        return _getLongestRoadSize.apply(state);
    }
}

