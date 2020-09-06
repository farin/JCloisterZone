package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import io.vavr.collection.Set;

public class LittleBuildingAction extends AbstractPlayerAction<LittleBuilding> {

    private final Position pos;

    public LittleBuildingAction(Set<LittleBuilding> options, Position pos) {
        super(options);
        this.pos = pos;
    }

    public Position getPosition() {
        return pos;
    }
}
