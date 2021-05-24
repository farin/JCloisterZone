package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.feature.ModifiedFeature;
import com.jcloisterzone.game.setup.SetupQuery;

public class BooleanOrModifier extends FeatureModifier<Boolean>{

    public BooleanOrModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    };

    @Override
    public Boolean mergeValues(Boolean a, Boolean b) {
        return a || b;
    }

    @Override
    public Boolean valueOf(String attr) {
        return attr.equals("yes") || attr.equals("true") || attr.equals("1");
    }
}
