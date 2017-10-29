package com.jcloisterzone.event.play;

import com.jcloisterzone.board.Tile;

public class TileDiscardedEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final Tile tile;

    public TileDiscardedEvent(Tile tile) {
        super(PlayEventMeta.createWithoutPlayer());
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

}
