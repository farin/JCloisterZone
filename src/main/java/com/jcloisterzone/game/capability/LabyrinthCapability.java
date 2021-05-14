package com.jcloisterzone.game.capability;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.modifier.BooleanModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class LabyrinthCapability extends Capability<Void> {

    public static BooleanModifier LABYRINTH = new BooleanModifier("labyrinth");

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if ("advanced".equals(state.getStringRule(Rule.LABYRINTH_VARIANT))) {
            if (feature instanceof Road) {
                if (attributeBoolValue(xml, "labyrinth")) {
                    feature = ((Road) feature).putModifier(LABYRINTH, true);
                }
            }
        }
        return feature;
    }
}
