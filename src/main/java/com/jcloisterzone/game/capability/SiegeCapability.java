package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;


public final class SiegeCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

	public static final TileModifier SIEGE_ESCAPE_TILE = new TileModifier("SiegeEscapeTile");

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        if (!XMLUtils.getElementStreamByTagName(tileElement, "city")
                .filter(cityEl -> attributeBoolValue(cityEl, "besieged"))
                .isEmpty()) {
            tile = tile.addTileModifier(SIEGE_ESCAPE_TILE);
        }
        return tile;
    }
}
