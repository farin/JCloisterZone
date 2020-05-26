package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.RemoveTileException;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Vector;

public class GermanMonasteriesCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Cloister && attributeBoolValue(xml, "monastery")) {
            feature = ((Cloister)feature).setMonastery(true);
        }
        return feature;
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) throws RemoveTileException {
        if (!state.getBooleanValue(Rule.KEEP_CLOISTERS)) {
            if (tile.getId().equals("BA.L") || tile.getId().equals("BA.LR")) {
                throw new RemoveTileException();
            }
        }
        return tile;
    }
}
