package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

public class CornCircleSelectOptionEvent extends PlayEvent {

    public CornCircleSelectOptionEvent(Player player, Position position) {
        super(player, position);
    }

}
