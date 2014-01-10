package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;

public class BridgeDeployedEvent extends Event {

    public BridgeDeployedEvent(Player player, Position position, Location location) {
        super(player, position, location);
    }

}
