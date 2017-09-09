package com.jcloisterzone.game.capability;


import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Queue;

public class BazaarCapability extends Capability<BazaarCapabilityModel> {

    @Override
    public Tile initTile(GameState state, Tile tile, Element xml) {
        if (xml.getElementsByTagName("bazaar").getLength() > 0) {
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