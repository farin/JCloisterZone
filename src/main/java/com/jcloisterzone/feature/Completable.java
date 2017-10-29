package com.jcloisterzone.feature;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Set;

public interface Completable extends Scoreable {

    boolean isOpen(GameState state);
    default boolean isCompleted(GameState state) {
        return !isOpen(state);
    }

    Completable setNeighboring(Set<FeaturePointer> neighboring);
    Set<FeaturePointer> getNeighboring();

    int getPoints(GameState state);
}
