package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;

public class TileDrawnEvent extends Event {

    private final Tile tile;

    public TileDrawnEvent(Player player, Tile tile) {
        super(player, tile.getPosition());
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

}
