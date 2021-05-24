package com.jcloisterzone.game.capability;

import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.modifier.BooleanOrModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class CathedralCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;



    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if ((feature instanceof City) && attributeBoolValue(xml, "cathedral")) {
            feature = ((City) feature).putModifier(City.CATHEDRAL, true);
        }
        return feature;
    }
}
