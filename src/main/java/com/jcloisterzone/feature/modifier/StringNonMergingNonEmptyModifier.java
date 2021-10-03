package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public class StringNonMergingNonEmptyModifier extends FeatureModifier<String> {

    public StringNonMergingNonEmptyModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public String mergeValues(String a, String b) {
    	if (a == null) {
    		return b;
    	} else if (b == null) {
    		return null;
    	} else {
    		return (a.equals(b) ? a : "");
    	}
    }

    @Override
    public String valueOf(String attr) {
        return attr;
    }
}
