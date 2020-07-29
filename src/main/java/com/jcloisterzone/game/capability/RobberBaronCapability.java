package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenReceivedEvent;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Road;
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

public final class RobberBaronCapability extends Capability<Void> {

    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
        Set<Scoreable> completedFeatures = completed.keySet();
        int maxRoadSize = getMaxSize(state, Road.class, completedFeatures);
        Road longestRoadCompleted = null;

        for (Scoreable feature : completed.keySet()) {
            if (feature instanceof Road && feature.getTilePositions().size() > maxRoadSize) {
                longestRoadCompleted = (Road) feature;
            }
        }

        Player turnPlayer = state.getTurnPlayer();
        PlayersState ps = state.getPlayers();
        if (longestRoadCompleted != null) {
            for (Player p : ps.getPlayers()) {
                ps = ps.setTokenCount(p.getIndex(), BiggestFeatureAward.ROBBER, p.equals(turnPlayer) ? 1 : 0);
            }
            TokenReceivedEvent ev = new TokenReceivedEvent(
                    PlayEventMeta.createWithActivePlayer(state), turnPlayer, BiggestFeatureAward.ROBBER, 1);
            ev.setSourceFeature(longestRoadCompleted);
            state = state.appendEvent(ev);
        }
        return state.setPlayers(ps);
    }

    @Override
    public GameState onFinalScoring(GameState state) {
        PlayersState ps = state.getPlayers();

        for (Player player: ps.getPlayers()) {
            if (ps.getPlayerTokenCount(player.getIndex(), BiggestFeatureAward.ROBBER) > 0) {
                state = (new AddPoints(player, countCompletedRoads(state))).apply(state);
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

