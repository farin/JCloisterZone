package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.feature.ModifiedFeature;
import com.jcloisterzone.game.setup.SetupQuery;

public class IntegerAddModifier extends FeatureModifier<Integer> {

    public IntegerAddModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public Integer mergeValues(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public Integer valueOf(String attr) {
        return Integer.parseInt(attr);
    }
}