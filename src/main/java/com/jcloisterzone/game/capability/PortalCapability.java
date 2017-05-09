package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;

@Immutable
public class PortalCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("portal").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.PORTAL);
        }
        return tile;
    }
}
