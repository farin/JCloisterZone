package com.jcloisterzone.event.play;

import com.jcloisterzone.board.TileDefinition;

public class TileDiscardedEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final TileDefinition tile;

    public TileDiscardedEvent(TileDefinition tile) {
        super(PlayEventMeta.createWithoutPlayer());
        this.tile = tile;
    }

    public TileDefinition getTile() {
        return tile;
    }

}
