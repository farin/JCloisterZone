package com.jcloisterzone.ai;

import java.util.Comparator;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.GameStatePhaseReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.Tuple2;
import io.vavr.collection.SortedMap;

public class RankingAiPlayer implements AiPlayer {

    private GameStateRanking stateRanking;
    private GameStatePhaseReducer phaseReducer;
    private Comparator<Double> rankingComparator;

    public RankingAiPlayer(GameStateRanking stateRanking) {
        this.stateRanking = stateRanking;
        this.rankingComparator = (a, b) -> Double.compare(b, a);
    }

    @Override
    public void onGameStart(Config config, GameSetup setup) {
        phaseReducer = new GameStatePhaseReducer(config, setup, 0);
    }

    @Override
    public WsInGameMessage apply(GameState state) {
        SortedMap<Double, WsInGameMessage> messages = getPossibleActions(state)
            .toSortedMap(rankingComparator, msg -> {
                GameState newState = phaseReducer.apply(state, msg);
                Double ranking = stateRanking.apply(newState);
                return new Tuple2<>(ranking, msg);
            });

        return messages.get()._2;
    }

}
