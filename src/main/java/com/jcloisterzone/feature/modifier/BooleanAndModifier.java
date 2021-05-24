package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public class BooleanAndModifier extends BooleanOrModifier {

    public BooleanAndModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public Boolean mergeValues(Boolean a, Boolean b) {
        return a && b ? true : null; // strip modifier if is not set
    }
}
