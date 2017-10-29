package com.jcloisterzone.feature;

public interface MultiTileFeature<T extends MultiTileFeature<?>> extends Feature {

    T merge(T f);
}
