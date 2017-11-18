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
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

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

    private GameState scorePlayer(GameState state, Player p, Follower nextToFairy, boolean finalScoring) {
        int points = getFeaturePoints(state, p);
        PointCategory pointCategory = feature.getPointCategory();

        state = (new AddPoints(p, points, pointCategory)).apply(state);

        Follower follower = nextToFairy == null ? feature.getSampleFollower(state, p) : nextToFairy;
        ScoreEvent scoreEvent;


        if (nextToFairy != null && !finalScoring) {
            state = (new AddPoints(
                p, FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY
            )).apply(state);

            scoreEvent = new ScoreEvent(
                points+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT,
                points+" + "+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT,
                pointCategory,
                finalScoring,
                follower.getDeployment(state),
                follower
            );
        } else {
            scoreEvent = new ScoreEvent(
                points,
                pointCategory,
                finalScoring,
                follower.getDeployment(state),
                follower
            );
        }

        state = state.appendEvent(scoreEvent);
        return state;
    }

    @Override
    public GameState apply(GameState state) {
        owners = feature.getOwners(state);
        if (owners.isEmpty()) {
            Stream<Tuple2<Follower, FeaturePointer>> followers = feature.getFollowers2(state);
            if (!followers.isEmpty()) {
                for (Seq<Tuple2<Follower, FeaturePointer>> l : followers.groupBy(t -> t._1.getPlayer()).values()) {
                    Tuple2<Follower, FeaturePointer> t = l.get();
                    ScoreEvent scoreEvent = new ScoreEvent(0, feature.getPointCategory(), isFinal, t._2, t._1);
                    state = state.appendEvent(scoreEvent);
                }
            }
            return state;
        }

        HashMap<Player, Follower> playersWithFairyBonus = HashMap.empty();

        BoardPointer ptr = state.getNeutralFigures().getFairyDeployment();
        if (ptr != null && !isFinal) {
            boolean onTileRule = ptr instanceof Position;
            FeaturePointer fairyFp = ptr.asFeaturePointer();

            for (Tuple2<Follower, FeaturePointer> t : feature.getFollowers2(state)) {
                Follower m = t._1;
                if (!t._2.equals(fairyFp)) continue;

                if (!onTileRule) {
                    if (!((MeeplePointer) ptr).getMeepleId().equals(m.getId())) continue;
                }

                playersWithFairyBonus = playersWithFairyBonus.put(m.getPlayer(), m);
            }
        }

        for (Player pl : owners) {
            Follower nextToFairy = playersWithFairyBonus.get(pl).getOrNull();
            state = scorePlayer(state, pl, nextToFairy, isFinal);
        }

        for (Player pl : playersWithFairyBonus.keySet().removeAll(owners)) {
            // player is not owner but next to fairy -> add just fairy points
            Follower nextToFairy = playersWithFairyBonus.get(pl).getOrNull();

            state = (new AddPoints(
                pl, FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY
            )).apply(state);

            ScoreEvent scoreEvent = new ScoreEvent(
                FairyCapability.FAIRY_POINTS_FINISHED_OBJECT,
                PointCategory.FAIRY,
                false,
                nextToFairy.getDeployment(state),
                nextToFairy
            );
            state = state.appendEvent(scoreEvent);
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
}
