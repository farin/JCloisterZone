package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PointsExpression;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.event.play.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.awt.*;

/** Score feature followers */
public abstract class ScoreFeature implements ScoreFeatureReducer {

    private final Scoreable feature;
    protected final boolean isFinal;

    // "out" variable - computed owners are store to instance
    // to be available to reducer caller
    private Set<Player> owners;

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


    @Override
    public GameState apply(GameState state) {
        owners = feature.getOwners(state);

        List<ReceivedPoints> bonusPoints = List.empty();
        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            bonusPoints = cap.appendBonusPoints(state, bonusPoints, feature, isFinal);
        }

        Map<Player, List<ReceivedPoints>> playerPoints = HashMap.empty();

        if (owners.isEmpty()) {
            // not owners but still followers can exist - eg. Mayor on city without pennants
            //Stream<Tuple2<Follower, FeaturePointer>> followers = feature.getFollowers2(state);
            for (Player player : feature.getFollowers(state).map(Follower::getPlayer).distinct()) {
                PointsExpression expr = new PointsExpression(0, feature.getClass().getSimpleName().toLowerCase() + ".empty");
                playerPoints = playerPoints.put(player, List.of(new ReceivedPoints( expr, player, null)));
            }
        } else {
            for (Player player : owners) {
                PointsExpression expr = getFeaturePoints(state, player);
                state = (new AddPoints(player, expr.getPoints())).apply(state);
                playerPoints = playerPoints.put(player, List.of(new ReceivedPoints(expr, player, null)));
            }
        }

        for (ReceivedPoints bonus : bonusPoints) {
            Player player = bonus.getPlayer();
            state = (new AddPoints(player, bonus.getPoints())).apply(state);
            List<ReceivedPoints> sources = playerPoints.get(player).getOrElse(List.empty());
            playerPoints = playerPoints.put(player, sources.append(bonus));
        }

        List<ReceivedPoints> mergedReceivedPoints = List.empty();

        for (Tuple2<Player, List<ReceivedPoints>> t: playerPoints) {
            Player player = t._1;
            List<ReceivedPoints> sources = t._2;
            FeaturePointer sample = (FeaturePointer) sources.filter(s -> s.getSource() != null).map(ReceivedPoints::getSource).getOrNull();
            if (sample == null) {
                sample = feature.getSampleFollower2(state, player)._2;
            }
            sources = setSample(sources, sample);

            for (Tuple2<BoardPointer, List<ReceivedPoints>> g : sources.groupBy(ReceivedPoints::getSource)) {
                PointsExpression expr = null;
                for (ReceivedPoints rp : g._2) {
                    expr = expr == null ? rp.getExpression() : expr.merge(rp.getExpression());
                }

                mergedReceivedPoints = mergedReceivedPoints.append(new ReceivedPoints(expr, player, g._1));
            }
        }

        if (!mergedReceivedPoints.isEmpty()) {
            state = state.appendEvent(new ScoreEvent(mergedReceivedPoints, true, isFinal));
        }

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
