package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.Set;

public class CastleAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public CastleAction(Set<FeaturePointer> options) {
        super(options);
    }

}
