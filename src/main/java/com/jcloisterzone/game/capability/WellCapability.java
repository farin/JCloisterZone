package com.jcloisterzone.game.capability;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.modifier.IntegerAddModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeIntValue;

public class WellCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;



    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Road) {
            int wells = attributeIntValue(xml, "wells", 0);
            if (wells > 0) {
                feature = ((Road) feature).putModifier(Road.WELLS, wells);
            }
        }
        return feature;
    }
}