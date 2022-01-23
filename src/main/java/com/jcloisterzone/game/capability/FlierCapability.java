package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.FlyingMachine;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class FlierCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        Vector<Element> flyingMachineEl = XMLUtils.getElementStreamByTagName(tileElement, "flying-machine").toVector();
        if (flyingMachineEl.size() == 0) {
            return tile;
        }
        if (flyingMachineEl.size() == 1) {
            Location direction = Location.valueOf(flyingMachineEl.get().getAttribute("direction"));
            FeaturePointer fp = new FeaturePointer(Position.ZERO, FlyingMachine.class, Location.I);
            FlyingMachine feature = new FlyingMachine(fp, direction);
            return tile.setInitialFeatures(tile.getInitialFeatures().put(fp, feature));
        }
        throw new IllegalStateException("multiple <flying-machine> elements");
    }
}
