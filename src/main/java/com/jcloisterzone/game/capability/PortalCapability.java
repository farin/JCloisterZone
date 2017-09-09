package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class PortalCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Element xml) {
        if (xml.getElementsByTagName("portal").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.PORTAL);
        }
        return tile;
    }
}
