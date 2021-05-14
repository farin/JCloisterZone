package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.RemoveTileException;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.modifier.BooleanModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class MonasteriesCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    public static final BooleanModifier MONASTERY = new BooleanModifier("monastery");

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Cloister && attributeBoolValue(xml, "monastery")) {
            feature = ((Cloister)feature).putModifier(MONASTERY, true);
        }
        return feature;
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) throws RemoveTileException {
        if ("replace".equals(state.getStringRule(Rule.KEEP_MONASTERIES))) {
            if (tile.getId().equals("BA/L") || tile.getId().equals("BA/LR")) {
                throw new RemoveTileException();
            }
        }
        return tile;
    }
}
