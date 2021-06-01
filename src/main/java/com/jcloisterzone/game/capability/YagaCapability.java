package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.YagaHut;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class YagaCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        Vector<Element> yagaEl = XMLUtils.getElementStreamByTagName(tileElements, "yaga-hut").toVector();
        assert yagaEl.size() <= 1;
        if (yagaEl.size() > 0) {
            YagaHut feature =  new YagaHut();
            tile = tile.setInitialFeatures(tile.getInitialFeatures().put(Location.MONASTERY, feature));
        }
        return tile;
    }
}
