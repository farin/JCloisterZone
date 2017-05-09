package com.jcloisterzone.feature;

public interface MultiTileFeature<T extends MultiTileFeature<?>> extends Scoreable {

    T merge(T f);
}
