package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

@Deprecated  //? use meeple event instead
public class FairyMovedEvent extends Event {

    public FairyMovedEvent(Player player, Position position) {
        super(player, position);
    }



}
