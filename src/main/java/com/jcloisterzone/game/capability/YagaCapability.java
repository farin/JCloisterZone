package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.YagaHut;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;

public class YagaCapability extends Capability<Void> {

    @Override
    public Tile initTile(GameState state, Tile tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("yaga-hut");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            YagaHut feature =  new YagaHut();
            return tile.setInitialFeatures(tile.getInitialFeatures().put(Location.CLOISTER, feature));
        }
        return tile;
    }
}
