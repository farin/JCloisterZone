package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.feature.ModifiedFeature;
import com.jcloisterzone.game.setup.SetupQuery;

public class BooleanAnyModifier extends BooleanModifier {

    public BooleanAnyModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    };

    @Override
    public Boolean mergeValues(Boolean a, Boolean b) {
        return a || b;
    }
}
