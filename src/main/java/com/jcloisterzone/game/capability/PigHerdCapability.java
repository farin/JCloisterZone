package com.jcloisterzone.game.capability;

import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.modifier.IntegerAddModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class PigHerdCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Farm) {
            if (attributeBoolValue(xml, "pig")) {
                if ("pig".equals(state.getStringRule(Rule.GQ11_PIG_HERD)) || !"GQ/F".equals(tileId)) {
                    feature = ((Farm) feature).putModifier(Farm.PIG_HERD, 1);
                }
            }
        }
        return feature;
    }
}
