package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;

public class TileEvent extends PlayEvent {

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

}
