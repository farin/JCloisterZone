package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

/**
 * A feature on which can be meeple deployed (this includes also Field feature).
 */
public interface Structure extends Feature {

    default Stream<Tuple2<Meeple, FeaturePointer>> getMeeples2(GameState state) {
        Set<FeaturePointer> fps = HashSet.ofAll(getPlaces());
        return Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> fps.contains(t._2));
    }

    default Stream<Meeple> getMeeples(GameState state) {
        return getMeeples2(state).map(t -> t._1);
    }

    default Stream<Tuple2<Follower, FeaturePointer>> getFollowers2(GameState state) {
        return getMeeples2(state)
            .filter(t -> t._1 instanceof Follower)
            .map(t -> t.map1(f -> (Follower) f));
    }

    default Stream<Follower> getFollowers(GameState state) {
        return getFollowers2(state).map(t -> t._1);
    }

    default Stream<Tuple2<Special, FeaturePointer>> getSpecialMeeples2(GameState state) {
        return getMeeples2(state)
            .filter(t -> t._1 instanceof Special)
            .map(t -> t.map1(m -> (Special) m));
    }

    default Stream<Special> getSpecialMeeples(GameState state) {
        return getSpecialMeeples2(state).map(t -> t._1);
    }


    default boolean isOccupied(GameState state) {
        return !getMeeples(state).isEmpty();
    }

    default boolean isOccupiedBy(GameState state, Player p) {
        return !getFollowers(state).find(m -> m.getPlayer().equals(p)).isEmpty();
    }
}
