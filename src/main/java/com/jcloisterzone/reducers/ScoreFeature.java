package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.engine.Game;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

/** Score feature followers */
public abstract class ScoreFeature implements ScoreFeatureReducer {

    private final Scoreable feature;
    protected final boolean isFinal;

    // "out" variable - computed owners are store to instance
    // to be available to reducer caller
    private Set<Player> owners;
    private List<ReceivedPoints> bonusPoints = List.empty();

    public ScoreFeature(Scoreable feature, boolean isFinal) {
        this.feature = feature;
        this.isFinal = isFinal;
    }

    abstract protected PointsExpression getFeaturePoints(GameState state, Player player);


    private List<ReceivedPoints> setSample(List<ReceivedPoints> sources, FeaturePointer sample) {
        return sources.map(rp -> {
            if (rp.getSource() == null) {
                return new ReceivedPoints(rp.getExpression(), rp.getPlayer(), sample);
            }
            return rp;
        });
    }


    private BoardPointer getSampleSource(GameState state, Player player, List<ReceivedPoints> bonusPoints) {
        List<Tuple2<Follower, FeaturePointer>> followers = feature.getFollowers2(state).filter(t -> t._1.getPlayer().equals(player)).toList();
        for (ReceivedPoints bonus : bonusPoints) {
            if (!bonus.getPlayer().equals(player)) {
                continue;
            }
            for (Tuple2<Follower, FeaturePointer> t : followers) {
                if (t._2.equals(bonus.getSource())) {
                    return t._2;
                }
            }
        }
        return followers.get()._2;
    }

    protected GameState addFiguresBonusPoints(GameState state) {
        for (ReceivedPoints bonus : bonusPoints) {
            Player player = bonus.getPlayer();
            state = (new AddPoints(player, bonus.getPoints())).apply(state);
        }

        for (Tuple2<String, List<ReceivedPoints>> t : bonusPoints.groupBy(bonus -> bonus.getExpression().getName())) {
            state = state.appendEvent(new ScoreEvent(t._2, false, isFinal));
        }

        return state;
    }

    @Override
    public GameState apply(GameState state) {
        owners = feature.getOwners(state);

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            bonusPoints = cap.appendFiguresBonusPoints(state, bonusPoints, feature, isFinal);
        }

        List<ReceivedPoints> receivedPoints = List.empty();

        if (owners.isEmpty()) {
            // not owners but still followers can exist - eg. Mayor on city without pennants
            //Stream<Tuple2<Follower, FeaturePointer>> followers = feature.getFollowers2(state);
            for (Player player : feature.getFollowers(state).map(Follower::getPlayer).distinct()) {
                PointsExpression expr = new PointsExpression(feature.getClass().getSimpleName().toLowerCase() + ".empty", List.empty());
                receivedPoints = receivedPoints.append(new ReceivedPoints( expr, player, getSampleSource(state, player, bonusPoints)));
            }
        } else {
            for (Player player : owners) {
                PointsExpression expr = getFeaturePoints(state, player);
                state = (new AddPoints(player, expr.getPoints())).apply(state);
                receivedPoints = receivedPoints.append(new ReceivedPoints(expr, player, getSampleSource(state, player, bonusPoints)));
            }
        }

        if (!receivedPoints.isEmpty()) {
            state = state.appendEvent(new ScoreEvent(receivedPoints, true, isFinal));
        }

        state = addFiguresBonusPoints(state);
        return state;
    }

    @Override
    public Scoreable getFeature() {
        return feature;
    }

    @Override
    public Set<Player> getOwners() {
        return owners;
    }

    static class ScoreEventSource {
        private int points;
        private Follower follower;
        private List<Position> source;


        public ScoreEventSource(int points, Follower follower, List<Position> source) {
            this.points = points;
            this.follower = follower;
            this.source = source;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public Follower getFollower() {
            return follower;
        }

        public void setFollower(Follower follower) {
            this.follower = follower;
        }

        public List<Position> getSource() {
            return source;
        }

        public void setSource(List<Position> source) {
            this.source = source;
        }
    }
}
