package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.DefaultTilePack;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;

public class TileEvent extends PlayEvent implements Undoable {

    public static final int DRAW = 1;
    public static final int PLACEMENT = 2;
    public static final int DISCARD = 3;

    private final Tile tile;

    public TileEvent(int type, Tile tile) {
        this(type, null, tile);
    }

    public TileEvent(int type, Player player, Tile tile) {
        super(type, player, tile.getPosition());
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    @Override
    public void undo(Game game) {
        switch (getType()) {
        case PLACEMENT:
            game.getBoard().unmergeFeatures(tile);
            game.getBoard().remove(tile);
            if (tile.isAbbeyTile()) {
                tile.setRotation(Rotation.R0);
                game.setCurrentTile(null);
                ((DefaultTilePack)game.getTilePack()).addTile(tile, TilePack.INACTIVE_GROUP);
            }
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }

}
