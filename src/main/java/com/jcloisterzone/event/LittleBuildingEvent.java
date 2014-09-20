package com.jcloisterzone.event;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;

public class LittleBuildingEvent extends PlayEvent {

    private final LittleBuilding building;

    public LittleBuildingEvent(Player triggeringPlayer, LittleBuilding building) {
        super(triggeringPlayer, null);
        this.building = building;
    }

    public LittleBuilding getBuilding() {
        return building;
    }

}
