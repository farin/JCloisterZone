package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

public class FlierRollEvent extends PlayEvent {

    private final int distance;

    public FlierRollEvent(Player player, Position position, int distance) {
        super(player, position);
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }
}
