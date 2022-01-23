package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public class BooleanAllModifier extends BooleanModifier {

    public BooleanAllModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public boolean isExclusive(Boolean a) {
        return true;
    }

    @Override
    public Boolean mergeValues(Boolean a, Boolean b) {
        return a && b ? true : null; // strip modifier if is not set
    }
}
