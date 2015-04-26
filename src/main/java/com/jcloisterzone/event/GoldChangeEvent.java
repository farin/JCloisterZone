package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

public class GoldChangeEvent extends PlayEvent {

    private final Position pos;
    private final int count;

    public GoldChangeEvent(Player triggeringPlayer, Position pos, int count) {
        super(triggeringPlayer, null);
        this.pos = pos;
        this.count = count;
    }

    public Position getPos() {
        return pos;
    }

    public int getCount() {
        return count;
    }
}
