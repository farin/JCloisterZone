package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.Set;

public class AcrobatsScoreAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public AcrobatsScoreAction(Set<FeaturePointer> options) {
        super(options);
    }

}
