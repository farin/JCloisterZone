package com.jcloisterzone.feature;

public interface MultiTileFeature<T extends MultiTileFeature<?>> extends EdgeFeature<T> {
    T merge(T f);
}
