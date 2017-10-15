package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Vector;

@Immutable
public class PortalCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "portal").isEmpty()) {
            tile = tile.setTileTrigger(TileTrigger.PORTAL);
        }
        return tile;
    }
}
