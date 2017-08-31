package com.jcloisterzone.feature;


import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public interface Feature {

    List<FeaturePointer> getPlaces();
    Feature placeOnBoard(Position pos, Rotation rot);
    Stream<Tuple2<Meeple, FeaturePointer>> getMeeples2(GameState state);

    default Set<Position> getTilePositions() {
        return getPlaces().map(fp -> fp.getPosition()).toSet();
    }

    default Stream<Meeple> getMeeples(GameState state) {
        return getMeeples2(state).map(t -> t._1);
    }

    default Stream<Tuple2<Follower, FeaturePointer>> getFollowers2(GameState state) {
        return
            Stream.narrow(
                getMeeples2(state)
                .filter(t -> t._1 instanceof Follower)
            );
    }

    default Stream<Follower> getFollowers(GameState state) {
        return getFollowers2(state).map(t -> t._1);
    }

    default Stream<Tuple2<Special, FeaturePointer>> getSpecialMeeples2(GameState state) {
        return
            Stream.narrow(
                getMeeples2(state)
                .filter(t -> t._1 instanceof Special)
            );
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
