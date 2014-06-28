package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;

public class BridgeDeployedEvent extends FeatureEvent {

    public BridgeDeployedEvent(Player player, Position position, Location location) {
        super(player, new FeaturePointer(position, location));
    }

}
