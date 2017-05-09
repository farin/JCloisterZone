package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.grid.layer.FollowerAreaLayer;

import io.vavr.collection.Map;
import io.vavr.collection.Set;


@LinkedGridLayer(FollowerAreaLayer.class)
public abstract class SelectFollowerAction extends PlayerAction<MeeplePointer> {

    public SelectFollowerAction(Set<MeeplePointer> options) {
        super(options);
    }

    //temporary legacy, TODO direct meeple selection on client

    public Map<FeaturePointer, Set<MeeplePointer>> groupByFeaturePointer() {
        return Map.narrow(
            getOptions().groupBy(mp -> mp.asFeaturePointer())
        );
    }
}
