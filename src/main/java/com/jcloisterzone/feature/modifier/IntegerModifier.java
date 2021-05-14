package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.feature.ModifiedFeature;

public class IntegerModifier extends FeatureModifier<Integer> {

    public IntegerModifier(String name) {
        super(name);
    }

    @Override
    public Integer mergeValues(Integer a, Integer b) {
        return a + b;
    }
}