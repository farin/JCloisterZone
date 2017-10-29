package com.jcloisterzone.event.play;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;

public class TilePlacedEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final Tile tile;
    private final Position position;
    private final Rotation rotation;

    public TilePlacedEvent(PlayEventMeta metadata, Tile tile, Position position, Rotation rotation) {
        super(metadata);
        this.tile = tile;
        this.position = position;
        this.rotation = rotation;
    }

    public Tile getTile() {
        return tile;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }
}
