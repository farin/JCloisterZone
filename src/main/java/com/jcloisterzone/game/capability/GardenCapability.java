package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Garden;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class GardenCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        if (!XMLUtils.getElementStreamByTagName(tileElement, "garden").isEmpty()) {
            Garden garden = new Garden();
            tile = tile.setInitialFeatures(tile.getInitialFeatures().put(garden.getPlace(), garden));
        }
        return tile;
    }
}