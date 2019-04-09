package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.LittleBuildingActionLayer;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;

@LinkedImage("actions/building")
@LinkedGridLayer(LittleBuildingActionLayer.class)
public class LittleBuildingAction extends AbstractPlayerAction<LittleBuilding> {

    private final Position pos;

    public LittleBuildingAction(Set<LittleBuilding> options, Position pos) {
        super(options);
        this.pos = pos;
    }

    @Override
    public WsInGameMessage select(LittleBuilding option) {
        return new PlaceTokenMessage(option, pos);
    }

    public Position getPosition() {
        return pos;
    }
}
