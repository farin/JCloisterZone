package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroup;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;


/**
 * @model Integer: store playerIndex during final abbey placement turn
 */
public class AbbeyCapability extends Capability<Integer> {


    @Override
    public GameState onStartGame(GameState state) {
        return state.mapPlayers(ps -> ps.setTokenCountForAllPlayers(Token.ABBEY_TILE, 1));
    }

    @Override
    public String getTileGroup(Tile tile) {
        return tile.getId().equals(Tile.ABBEY_TILE_ID) ? TileGroup.INACTIVE_GROUP: null;
    }
}
