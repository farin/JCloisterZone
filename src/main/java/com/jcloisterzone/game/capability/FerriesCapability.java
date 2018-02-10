package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.Vector;

/* model is map of placed ferries */
public class FerriesCapability extends Capability<FerriesCapabilityModel> {

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "ferry").isEmpty()) {
            tile = tile.setTileTrigger(TileTrigger.FERRY);
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
