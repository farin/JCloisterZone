package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public class IntegerNonMergingModifier extends FeatureModifier<Integer> {

    public IntegerNonMergingModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public Integer mergeValues(Integer a, Integer b) {
        return null; // stip it after merge
    }

    @Override
    public Integer valueOf(String attr) {
        return Integer.parseInt(attr);
    }
}
