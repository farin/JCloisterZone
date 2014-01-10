package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

public class TowerIncreasedEvent extends Event {

    private final int captureRange;

    public TowerIncreasedEvent(Player player, Position position, int captureRange) {
        super(player, position);
        this.captureRange = captureRange;
    }

    public int getCaptureRange() {
        return captureRange;
    }

}
