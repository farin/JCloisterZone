package com.jcloisterzone.ai;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;

public interface GameRanking {

    double getPartialAfterTilePlacement(Game game, Tile tile);
    double getFinal(Game game);
}