package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import io.vavr.collection.Vector;

import org.w3c.dom.Element;

/** model contains boolean information of reverse player order */
public class CarouselCapability extends Capability<Boolean> {

	public static final TileModifier CAROUSEL = new TileModifier("Carousel");
	   
	private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        Vector<Element> carouselEl = XMLUtils.getElementStreamByTagName(tileElement, "carousel").toVector();
        if (carouselEl.size() == 0) {
            return tile;
        }
        if (carouselEl.size() == 1) {
          	tile = tile.addTileModifier(CAROUSEL);
            return tile;
        }
        throw new IllegalStateException("multiple <carousel> elements");
    }

    @Override
    public GameState onTilePlaced(GameState state, PlacedTile pt) {
        if (!pt.getTile().hasModifier(CAROUSEL)) {
            return state;
        }
        Boolean currentOrder = getModel(state);
        
    	state = setModel(state, (currentOrder == null || !currentOrder) ? true : false);
        
        return state.setNextPlayerIncrement(-1 * state.getNextPlayerIncrement());
    }
}
