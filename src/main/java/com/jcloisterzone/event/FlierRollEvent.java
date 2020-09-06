package com.jcloisterzone.event;

import com.jcloisterzone.board.Position;

public class FlierRollEvent extends PlayEvent {

    private Position position;
    private final int distance;

    public FlierRollEvent(PlayEventMeta metadata, Position position, int distance) {
        super(metadata);
        this.position = position;
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public Position getPosition() {
        return position;
    }
}
