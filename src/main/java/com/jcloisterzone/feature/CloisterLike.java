package com.jcloisterzone.feature;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.state.GameState;

/**
 * Feature completed when it is surrounded by eight land tiles.
 */
public interface CloisterLike extends Completable {

    default boolean isOpen(GameState state) {
        Position pos = getPlaces().get().getPosition();
        return state.getAdjacentAndDiagonalTiles2(pos).size() < 8;
    }

}
