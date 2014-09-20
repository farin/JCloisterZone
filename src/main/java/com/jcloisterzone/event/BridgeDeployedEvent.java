package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;

public class BridgeDeployedEvent extends FeatureEvent {

    public BridgeDeployedEvent(Player triggeringPlayer, Position position, Location location) {
        super(triggeringPlayer, new FeaturePointer(position, location));
    }

}
