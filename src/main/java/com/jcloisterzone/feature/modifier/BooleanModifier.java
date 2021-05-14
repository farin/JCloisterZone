package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.feature.ModifiedFeature;

public class BooleanModifier extends FeatureModifier<Boolean>{

    public BooleanModifier(String name) {
        super(name);
    }

    @Override
    public Boolean mergeValues(Boolean a, Boolean b) {
        return a || b;
    }
}
