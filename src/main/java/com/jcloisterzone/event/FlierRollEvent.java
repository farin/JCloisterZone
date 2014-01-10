package com.jcloisterzone.event;

import com.jcloisterzone.board.Position;

public class FlierRollEvent extends Event {

    private final int distance;

    public FlierRollEvent(Position position, int distance) {
        super(position);
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }
}
