package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XmlUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;

public class InnCapability extends Capability {

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof Road) {
            ((Road) feature).setInn(attributeBoolValue(xml, "inn"));
        }
    }
}
