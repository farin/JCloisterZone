package com.jcloisterzone.action;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.LittleBuildingActionLayer;

import io.vavr.collection.Set;

// TODO generic token action ?

@LinkedImage("actions/building")
@LinkedGridLayer(LittleBuildingActionLayer.class)
public class LittleBuildingAction extends PlayerAction<LittleBuilding> {

    public LittleBuildingAction(Set<LittleBuilding> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, LittleBuilding target) {
       //server.placeLittleBuilding(target);
       //TODO
    }

}
