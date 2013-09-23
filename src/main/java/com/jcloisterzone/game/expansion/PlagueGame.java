package com.jcloisterzone.game.expansion;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.GameExtension;

public class PlagueGame extends GameExtension {

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("plague").getLength() > 0) {
            tile.setTrigger(TileTrigger.PLAGUE);
        }
    }

}
