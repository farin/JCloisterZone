package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.FlyingMachine;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

public class FlierCapability extends Capability<Void> {

    @Override
    public TileDefinition initTile(GameState state, TileDefinition tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("flying-machine");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            Element el = (Element) nl.item(0);
            Location direction = Location.valueOf(el.getAttribute("direction"));
            FlyingMachine feature = new FlyingMachine(new FeaturePointer(Position.ZERO, Location.FLYING_MACHINE), direction);
            return tile.setInitialFeatures(tile.getInitialFeatures().put(Location.FLYING_MACHINE, feature));
        }
        return tile;
    }
}
