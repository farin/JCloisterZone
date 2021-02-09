package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.SoloveiRazboynik;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.DeployMeeple;
import io.vavr.Predicates;
import io.vavr.Tuple2;

public class RussianPromosTrapPhase extends Phase {

    public RussianPromosTrapPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        for (Feature razboynik : state.getFeatures().filter(Predicates.instanceOf(SoloveiRazboynik.class))) {
            Position pos = razboynik.getPlaces().get().getPosition();
            Road road = (Road) state.getFeatureMap().get(new FeaturePointer(pos, Location.WE)).getOrNull();
            if (road == null) {
                road = (Road) state.getFeatureMap().get(new FeaturePointer(pos, Location.NS)).getOrNull();
            }
            FeaturePointer target = new FeaturePointer(pos, Location.TOWER);
            for (Tuple2<Follower, FeaturePointer> t : road.getFollowers2(state)) {
                Follower meeple = t._1;
                state = (new DeployMeeple(meeple, target)).apply(state);
            }
        };
        return next(state);
    }
}
