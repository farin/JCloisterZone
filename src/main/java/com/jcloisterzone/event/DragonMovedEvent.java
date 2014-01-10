package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

@Deprecated
public class DragonMovedEvent extends Event {

    public DragonMovedEvent(Player player, Position position) {
        super(player, position);
    }

}
