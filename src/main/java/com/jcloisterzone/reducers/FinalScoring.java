package com.jcloisterzone.reducers;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Stream;

public class FinalScoring implements Reducer {

    private <T extends Scoreable> Stream<T> getOccupiedScoreables(GameState state, Class<T> cls) {
        return state.getFeatures(cls).filter(c -> c.isOccupied(state));
    }

    private int getContinuousRowSize(GameState state, Position beginning, Location direction) {
        Position pos = beginning.add(direction);
        int size = 0;
        while (state.getPlacedTiles().containsKey(pos)) {
            size++;
            pos = pos.add(direction);
        }
        return size;
    }

    private int getMonasteryPoints(GameState state, Position pos) {
        int points = 1;
        for (Location loc : Location.SIDES) {
            points += getContinuousRowSize(state, pos, loc);
        }
        return points;
    }

    @Override
    public GameState apply(GameState state) {
        for (Completable completable : getOccupiedScoreables(state, Completable.class)) {
            state = (new ScoreCompletable(completable)).apply(state);
        }

        for (Castle castle : getOccupiedScoreables(state, Castle.class)) {
            // no points for castles at the end
            state = (new ScoreCastle(castle, 0)).apply(state);
        }

        LinkedHashMap<Meeple, FeaturePointer> abbots = state.getDeployedMeeples()
                .filterValues(fp -> fp.getLocation() == Location.MONASTERY);
        for (Tuple2<Meeple, FeaturePointer> t : abbots) {
            Follower follower = (Follower) t._1;
            Position pos = t._2.getPosition();
            int points = getMonasteryPoints(state, pos);

            state = (new AddPoints(
                follower.getPlayer(), points, PointCategory.CLOISTER
            )).apply(state);

            ScoreEvent scoreEvent = new ScoreEvent(points, PointCategory.CLOISTER, true, t._2, follower);
            state = state.appendEvent(scoreEvent);
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
