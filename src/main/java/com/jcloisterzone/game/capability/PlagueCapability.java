package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.CapabilityController;

public class PlagueCapability extends CapabilityController {

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("plague").getLength() > 0) {
            tile.setTrigger(TileTrigger.PLAGUE);
        }
    }

}
