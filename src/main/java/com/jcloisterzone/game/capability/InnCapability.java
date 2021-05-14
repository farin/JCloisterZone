package com.jcloisterzone.game.capability;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.modifier.BooleanModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class InnCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    public static BooleanModifier INN = new BooleanModifier("inn");

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Road) {
            if (attributeBoolValue(xml, "inn")) {
                feature = ((Road) feature).putModifier(INN, true);
            }
        }
        return feature;
    }
}
