package com.jcloisterzone.event;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

public class LittleBuildingEvent extends PlayEvent {

    private final LittleBuilding building;
    private final Position position;

    public LittleBuildingEvent(Player triggeringPlayer, LittleBuilding building, Position pos) {
        super(triggeringPlayer, null);
        this.building = building;
        this.position = pos;
    }

    public LittleBuilding getBuilding() {
        return building;
    }

    public Position getPosition() {
		return position;
	}

}
