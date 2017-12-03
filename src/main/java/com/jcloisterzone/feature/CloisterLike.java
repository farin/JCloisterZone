package com.jcloisterzone.feature;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Map;
import io.vavr.collection.Seq;

/**
 * Any feature completed when it is surrounded by eight land tiles.
 */
public interface CloisterLike extends Completable {

    @Override
    default boolean isOpen(GameState state) {
        Position pos = getPlaces().get().getPosition();
        return state.getAdjacentAndDiagonalTiles2(pos).size() < 8;
    }

    @Override
    default int getStructurePoints(GameState state, boolean completed) {
        return getPoints(state);
    }

    @Override
    default int getLittleBuildingPoints(GameState state) {
        Map<Position, Token> buildings = state.getCapabilityModel(LittleBuildingsCapability.class);
        if (buildings == null) {
            return 0;
        }
        Position cloisterPos = getPlaces().get().getPosition();
        Seq<Token> buldingsSeq = buildings.filterKeys(pos ->
            Math.abs(pos.x - cloisterPos.x) <= 1 && Math.abs(pos.y - cloisterPos.y) <= 1
        ).values();

        return LittleBuildingsCapability.getBuildingsPoints(state, buldingsSeq);
    }

}
