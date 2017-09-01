package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;


public final class SiegeCapability extends Capability<Void> {

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && attributeBoolValue(xml, "besieged")) {
            City city = (City) feature;
            return city.setBesieged(true);
        }
        return feature;
    }

    @Override
    public TileDefinition initTile(GameState state, TileDefinition tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("city");
        boolean besieged = false;
        for (int i = 0; i < nl.getLength(); i++) {
            Element cityEl = ((Element) nl.item(i));
            if (attributeBoolValue(cityEl, "besieged")) {
                besieged = true;
                break;
            }
        }

        if (besieged) {
            return tile.setTileTrigger(TileTrigger.BESIEGED);
        }
        return tile;
    }
}
