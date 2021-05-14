package com.jcloisterzone.feature.modifier;

public abstract class FeatureModifier<T> {

    private String name;

    public FeatureModifier(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract T mergeValues(T a, T b);
}
