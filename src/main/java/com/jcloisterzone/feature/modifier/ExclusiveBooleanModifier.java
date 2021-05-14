package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.feature.ModifiedFeature;

public class ExclusiveBooleanModifier extends BooleanModifier {

    public ExclusiveBooleanModifier(String name) {
        super(name);
    }

    @Override
    public Boolean mergeValues(Boolean a, Boolean b) {
        return a && b ? true : null; // strip modifier if is not set
    }
}
