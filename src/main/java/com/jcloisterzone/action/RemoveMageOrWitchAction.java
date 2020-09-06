package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import io.vavr.collection.Set;

public class RemoveMageOrWitchAction extends AbstractPlayerAction<NeutralFigure<FeaturePointer>> {

    public RemoveMageOrWitchAction(Set<NeutralFigure<FeaturePointer>> options) {
        super(options);
    }

}
