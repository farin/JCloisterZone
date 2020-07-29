package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

@Immutable
public class PortalCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    public static final TileModifier MAGIC_PORTAL = new TileModifier("MagicPortal");

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "portal").isEmpty()) {
            tile = tile.addTileModifier(MAGIC_PORTAL);
        }
        return tile;
    }
}
