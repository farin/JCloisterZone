package com.jcloisterzone.event;

import com.jcloisterzone.board.Tile;

public class TileDiscardedEvent extends Event {

    private final Tile tile;

    public TileDiscardedEvent(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }


}
