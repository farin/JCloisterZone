package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.BonusPoints;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.*;

import java.util.Objects;

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

    abstract protected int getFeaturePoints(GameState state, Player player);


    @Override
    public GameState apply(GameState state) {
        PointCategory pointCategory = feature.getPointCategory();
        owners = feature.getOwners(state);

        List<BonusPoints> bonusPoints = List.empty();
        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            bonusPoints = cap.appendBonusPoints(state, bonusPoints, feature, isFinal);
        }

        Map<Player, List<ScoreEventSource>> playerPoints = HashMap.empty();

        if (owners.isEmpty()) {
            // not owners but still followers can exist - eg. Mayor on city without pennants
            //Stream<Tuple2<Follower, FeaturePointer>> followers = feature.getFollowers2(state);
            for (Player player : feature.getFollowers(state).map(Follower::getPlayer).distinct()) {
                playerPoints = playerPoints.put(player, List.of(new ScoreEventSource(0, null, null)));
            }
        } else {
            for (Player player : owners) {
                int points = getFeaturePoints(state, player);
                state = (new AddPoints(player, points, pointCategory)).apply(state);
                playerPoints = playerPoints.put(player, List.of(new ScoreEventSource(points, null, null)));
            }
        }

        for (BonusPoints bonus : bonusPoints) {
            Player player = bonus.getPlayer();
            state = (new AddPoints(player, bonus.getPoints(), bonus.getPointCategory())).apply(state);
            List<ScoreEventSource> sources = playerPoints.get(player).getOrElse(List.empty());
            playerPoints = playerPoints.put(player, sources.append(new ScoreEventSource(bonus.getPoints(), bonus.getFollower(), bonus.getSource())));
        }

        for (Tuple2<Player, List<ScoreEventSource>> t: playerPoints) {
            Player player = t._1;
            List<ScoreEventSource> sources = t._2;
            Follower sample = sources.filter(s -> s.getFollower() != null).map(ScoreEventSource::getFollower).getOrNull();
            if (sample == null) {
                sample = feature.getSampleFollower(state, player);
            }
            for (ScoreEventSource s : sources.filter(s -> s.getFollower() == null)) {
                s.setFollower(sample);
            }
            for (Tuple2<Follower, List<ScoreEventSource>> g : sources.groupBy(ScoreEventSource::getFollower)) {
                Follower follower = g._1;
                List<Integer> pointValues = g._2.map(ScoreEventSource::getPoints);
                int points = pointValues.sum().intValue();
                String label = String.join(" + ", pointValues.map(Objects::toString));
                ScoreEvent scoreEvent = new ScoreEvent(points, label, pointCategory, isFinal, follower.getDeployment(state), follower);

                // hack
                // when ScoreEvent is bound to Follower it means two things
                // 1. - points are displayed next to follwer on board (that's ok for darmstadt church bonus)
                // 2. - event panel displays follower's feture on hover (that's not ok) - source overrides this. Anyway implementation of this is bad
                //      whole ScoreEvent shoul be refactored - TODO do it with new 5.0.0 client
                List<Position> source = g._2.get().getSource();
                if (source != null) {
                    scoreEvent = scoreEvent.setSource(source);
                }
                state = state.appendEvent(scoreEvent);
            }
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
