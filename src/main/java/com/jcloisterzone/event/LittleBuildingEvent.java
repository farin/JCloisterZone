package com.jcloisterzone.event;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;

public class LittleBuildingEvent extends PlayEvent {

    private final LittleBuilding building;

    public LittleBuildingEvent(Player player, LittleBuilding building) {
        super(player);
        this.building = building;
    }

    public LittleBuilding getBuilding() {
        return building;
    }

}
