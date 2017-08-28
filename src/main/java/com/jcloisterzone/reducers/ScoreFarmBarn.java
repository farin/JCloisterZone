package com.jcloisterzone.reducers;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.Stream;

public class ScoreFarmBarn implements Reducer {

    private final Farm farm;

    public ScoreFarmBarn(Farm farm) {
        this.farm = farm;
    }

    @Override
    public GameState apply(GameState state) {
        Stream<Tuple2<Special, FeaturePointer>> barns = farm.getSpecialMeeples2(state)
            .filter(t -> t._1 instanceof Barn);

        int points = farm.getBarnPoints(state);
        PointCategory pointCategory = farm.getPointCategory();

        for (Tuple2<Special, FeaturePointer> t : barns) {
            Barn barn = (Barn) t._1;
            state = (new AddPoints(barn.getPlayer(), points, pointCategory)).apply(state);

            ScoreEvent scoreEvent = new ScoreEvent(
                points,
                pointCategory,
                true,
                t._2,
                barn
            );
            state = state.appendEvent(scoreEvent);
        }

        return state;
    }

}
