package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.event.play.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.*;

public class ScoreFarmBarn implements ScoreFeatureReducer {

    private final Farm farm;
    private final boolean isFinal;

    // "out" variable - computed owners are store to instance
    // to be available to reducer caller
    private Map<Player, Integer> playerPoints = HashMap.empty();

    public ScoreFarmBarn(Farm farm, boolean isFinal) {
        this.farm = farm;
        this.isFinal = isFinal;
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
        List<ReceivedPoints> receivedPoints = List.empty();

        for (Tuple2<Special, FeaturePointer> t : barns) {
            Barn barn = (Barn) t._1;
            state = (new AddPoints(barn.getPlayer(), points)).apply(state);
            playerPoints = playerPoints.put(barn.getPlayer(), points);

            receivedPoints = receivedPoints.append(new ReceivedPoints(points, null, barn.getPlayer(), t._2));
        }

        ScoreEvent scoreEvent = new ScoreEvent(receivedPoints, "farm.barn", true, isFinal);
        state = state.appendEvent(scoreEvent);

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
