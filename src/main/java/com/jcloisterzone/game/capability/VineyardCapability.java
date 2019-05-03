package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Vector;

public class VineyardCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

	public static final TileModifier VINEYARD = new TileModifier("Vineyard");

	@Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "vineyard").isEmpty()) {
            tile = tile.addTileModifier(VINEYARD);
        }
        return tile;
    }
}
