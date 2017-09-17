package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;

import io.vavr.collection.Set;

@LinkedGridLayer(FeatureAreaLayer.class)
public abstract class SelectFeatureAction extends PlayerAction<FeaturePointer> {

    private static final long serialVersionUID = 1L;

    public SelectFeatureAction(Set<FeaturePointer> options) {
        super(options);
    }
}
