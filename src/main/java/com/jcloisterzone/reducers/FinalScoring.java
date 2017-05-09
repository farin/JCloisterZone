package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.collection.Stream;

public class FinalScoring implements Reducer {

    private <T extends Scoreable> Stream<T> getOccupiedScoreables(GameState state, Class<T> cls) {
        return state.getFeatures(cls).filter(c -> c.isOccupied(state));
    }

    //AbbotScoring
//    public int getContinuousRowSize(Position start, Location direction) {
//        start = start.add(direction);
//        int size = 0;
//        while (getPlacedTile(start) != null) {
//            size++;
//            start = start.add(direction);
//        }
//        return size;
//    }

    @Override
    public GameState apply(GameState state) {
        for (Completable completable : getOccupiedScoreables(state, Completable.class)) {
            state = (new ScoreCompletable(completable)).apply(state);
        }

        for (Castle castle : getOccupiedScoreables(state, Castle.class)) {
            // no points for castles at the end
            state = (new ScoreCastle(castle, 0)).apply(state);
        }

        for (Farm farm : getOccupiedScoreables(state, Farm.class)) {
            boolean hasBarn = farm.getSpecialMeeples(state)
                .find(Predicates.instanceOf(Barn.class)).isDefined();
            if (hasBarn) {
                   state = (new ScoreFarmBarn(farm)).apply(state);
            } else {
                state = (new ScoreFarm(farm)).apply(state);
            }
        }

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.finalScoring(state);
        }

        return state;
    }

}
