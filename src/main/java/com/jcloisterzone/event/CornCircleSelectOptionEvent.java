package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.play.PlayEvent;


public class CornCircleSelectOptionEvent extends PlayEvent {

    private final Position position;

    public CornCircleSelectOptionEvent(Player targetPlayer, Position position) {
        super(null, targetPlayer);
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

}
