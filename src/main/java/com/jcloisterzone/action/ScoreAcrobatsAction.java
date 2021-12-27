package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.Set;

public class ScoreAcrobatsAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public ScoreAcrobatsAction(Set<FeaturePointer> options) {
        super(options);
    }
}
