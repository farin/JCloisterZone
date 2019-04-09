package com.jcloisterzone.game.capability;


import org.w3c.dom.Element;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Queue;
import io.vavr.collection.Vector;

public class BazaarCapability extends Capability<BazaarCapabilityModel> {

	private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "bazaar").isEmpty()) {
            tile = tile.setTileTrigger(TileTrigger.BAZAAR);
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