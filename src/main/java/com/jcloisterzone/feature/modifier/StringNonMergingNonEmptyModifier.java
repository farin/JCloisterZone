package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public class StringNonMergingNonEmptyModifier extends FeatureModifier<String> {

    public StringNonMergingNonEmptyModifier(String selector, SetupQuery enabledBy) {
        super(selector, enabledBy);
    }

    @Override
    public String mergeValues(String a, String b) {
    	System.out.println(a);
    	System.out.println(a == null ? "true" : "false");
    	System.out.println(b);
    	System.out.println(b == null ? "true" : "false");
    	if (a == null || b == null) {
        	System.out.println("null");
    		return null;
    	} else {
        	System.out.println(a.equals(b) ? a : "null");
    		return (a.equals(b) ? a : null);
    	}
    }

    @Override
    public String valueOf(String attr) {
        return attr;
    }
}
