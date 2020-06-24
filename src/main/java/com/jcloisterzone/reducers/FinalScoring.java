package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.*;
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
            state = (new ScoreCompletable(completable, true)).apply(state);
        }

        for (Castle castle : getOccupiedScoreables(state, Castle.class)) {
            // no points for castles at the end
            state = (new ScoreCastle(castle, 0, true)).apply(state);
        }

        Stream<Cloister> monasteries = state.getFeatures().filter(f -> f instanceof Cloister && ((Cloister) f).isMonastery()).map(f -> (Cloister) f);

        for (Cloister monastery: monasteries) {
            int points = getMonasteryPoints(state, monastery.getPosition());
            for (Player player : monastery.getMonasteryOwners(state)) {
                Follower follower = monastery.getMonasterySampleFollower(state, player);
                state = (new AddPoints(player, points, PointCategory.CLOISTER)).apply(state);
                ScoreEvent scoreEvent = new ScoreEvent(points, PointCategory.CLOISTER, true, monastery.getPlace(), follower);
                state = state.appendEvent(scoreEvent);
            }
        }

        for (Farm farm : getOccupiedScoreables(state, Farm.class)) {
            boolean hasBarn = farm.getSpecialMeeples(state)
                .find(Predicates.instanceOf(Barn.class)).isDefined();
            boolean hasFollowers = !farm.getFollowers(state).isEmpty();

            if (hasBarn) {
                if (hasFollowers) {
                    // special case, followers deployed using City of Carcassonne before final scoring
                    state = (new ScoreFarmWhenBarnIsConnected(farm)).apply(state);
                    state = (new UndeployMeeples(farm, false)).apply(state);
                }
                state = (new ScoreFarmBarn(farm, true)).apply(state);
            } else {
                if (hasFollowers) {
                    state = (new ScoreFarm(farm, true)).apply(state);
                }
            }
        }

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onFinalScoring(state);
        }

        return state;
    }

}
