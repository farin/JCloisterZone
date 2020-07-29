package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;


public final class SiegeCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

	public static final TileModifier SIEGE_ESCAPE_TILE = new TileModifier("SiegeEscapeTile");

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && attributeBoolValue(xml, "besieged")) {
            City city = (City) feature;
            return city.setBesieged(true);
        }
        return feature;
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "city")
                .filter(cityEl -> attributeBoolValue(cityEl, "besieged"))
                .isEmpty()) {
            tile = tile.addTileModifier(SIEGE_ESCAPE_TILE);
        }
        return tile;
    }
}
