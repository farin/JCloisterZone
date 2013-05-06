package com.jcloisterzone.game.expansion;

import org.w3c.dom.Element;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;


public final class RiverIIGame extends AbstractRiverGame {

    private static final String LAKE_ID = "R2.I.v";

    //if River 1 enabled. all is done in RiverGame class

    @Override
    public void initTile(Tile tile, Element xml) {
        if (!getGame().hasExpansion(Expansion.RIVER)) {
            super.initTile(tile, xml);
        }
    }

    @Override
    public boolean isPlacementAllowed(Tile tile, Position p) {
        if (!getGame().hasExpansion(Expansion.RIVER)) {
            return super.isPlacementAllowed(tile, p);
        }
        return true;
    }

    @Override
    protected String getLakeId() {
        return LAKE_ID;
    }

}
