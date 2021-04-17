package com.jcloisterzone.game.capability;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeIntValue;

public class WellCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Road) {
            feature = ((Road) feature).setModifier("wells", attributeIntValue(xml, "wells", 0));
        }
        return feature;
    }
}
