package com.jcloisterzone.ai.player;

import com.jcloisterzone.ai.GameStateRanking;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

class LegacyRanking implements GameStateRanking {
	
    @Override
    public Double apply(GameState state) {
        double ranking = 0.0;

        PlacedTile lastPlaced = state.getLastPlaced();

        ranking += 0.0001 * state.getAdjacentTiles2(lastPlaced.getPosition()).size();

        return ranking;
    }
}