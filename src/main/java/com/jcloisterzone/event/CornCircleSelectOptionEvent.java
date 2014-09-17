package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

@Idempotent
public class CornCircleSelectOptionEvent extends PlayEvent {

    private final Position position;

    public CornCircleSelectOptionEvent(Player targePlayer, Position position) {
        super(null, targePlayer);
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

}
