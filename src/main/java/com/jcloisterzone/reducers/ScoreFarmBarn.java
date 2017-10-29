package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class ScoreFarmBarn implements ScoreFeatureReducer {

    private final Farm farm;

    // "out" variable - computed owners are store to instance
    // to be available to reducer caller
    private Map<Player, Integer> playerPoints = HashMap.empty();

    public ScoreFarmBarn(Farm farm) {
        this.farm = farm;
    }

    @Override
    public Scoreable getFeature() {
        return farm;
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
            playerPoints = playerPoints.put(barn.getPlayer(), points);

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

    @Override
    public Set<Player> getOwners() {
        return playerPoints.keySet();
    }

    @Override
    public int getFeaturePoints() {
        throw new UnsupportedOperationException("Call getFeaturePoints() with player argument");
    }

    @Override
    public int getFeaturePoints(Player player) {
        return playerPoints.getOrElse(player, 0);
    }

}
