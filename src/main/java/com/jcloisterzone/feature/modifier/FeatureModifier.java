package com.jcloisterzone.feature.modifier;

import com.jcloisterzone.game.setup.SetupQuery;

public abstract class FeatureModifier<T> {

    private final String selector;
    private final String name;
    private final SetupQuery enabledBy;

    public FeatureModifier(String selector, SetupQuery enabledBy) {
        this.selector = selector;
        this.name = selector.replaceAll("\\w+\\[(\\w+)\\]", "$1");
        this.enabledBy = enabledBy;
    }

    public String getSelector() {
        return selector;
    }

    public String getName() {
        return name;
    }

    public SetupQuery getEnabledBy() {
        return enabledBy;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract T mergeValues(T a, T b);
    public abstract T valueOf(String attr);
}
