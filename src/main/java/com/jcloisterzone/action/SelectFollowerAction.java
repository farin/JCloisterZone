package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public abstract class SelectFollowerAction extends AbstractPlayerAction<MeeplePointer> {

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
