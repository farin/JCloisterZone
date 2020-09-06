package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.HashMap;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

/* model is map of placed ferries */
public class FerriesCapability extends Capability<FerriesCapabilityModel> {

	public static enum FerryToken implements Token {
		FERRY;
	}

	public static final TileModifier LAKE_FERRY = new TileModifier("LakeFerry");

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "ferry").isEmpty()) {
            tile = tile.addTileModifier(LAKE_FERRY);
        }
        return tile;
    }

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, new FerriesCapabilityModel());
    }

    @Override
    public GameState onTurnPartCleanUp(GameState state) {
        return updateModel(state, m -> m.setMovedFerries(HashMap.empty()));
    }
}
