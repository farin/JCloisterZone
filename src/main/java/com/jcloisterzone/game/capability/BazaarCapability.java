package com.jcloisterzone.game.capability;


import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Queue;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class BazaarCapability extends Capability<BazaarCapabilityModel> {

	private static final long serialVersionUID = 1L;

	public static final TileModifier BAZAAR = new TileModifier("Bazaar");

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "bazaar").isEmpty()) {
            tile = tile.addTileModifier(BAZAAR);
        }
        return tile;
    }

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, new BazaarCapabilityModel());
    }


    @Override
    public GameState onTurnCleanUp(GameState state) {
        return updateModel(state, model -> {
            Queue<BazaarItem> supply = model.getSupply();
            if (supply != null && supply.isEmpty()) {
                return model.setSupply(null);
            }
            return model;
        });
    }
}