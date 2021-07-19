package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public class StringNonMergingModifier extends FeatureModifier<String> {

    public StringNonMergingModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public String mergeValues(String a, String b) {
    	if (a != null) {
			return (b != null && a.equals(b)) ? a : null;
    	} else {
    		return (b != null) ? b : null;
    	}
    }

    @Override
    public String valueOf(String attr) {
        return attr;
    }
}
