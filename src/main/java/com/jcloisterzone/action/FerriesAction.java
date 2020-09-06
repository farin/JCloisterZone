package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.Set;

public class FerriesAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public FerriesAction(Set<FeaturePointer> options) {
        super(options);
    }

}
