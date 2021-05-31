package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public abstract class BooleanModifier extends FeatureModifier<Boolean> {
    public BooleanModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public Boolean valueOf(String attr) {
        return attr.equals("yes") || attr.equals("true") || attr.equals("1");
    }
}
