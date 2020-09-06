package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import io.vavr.collection.Set;

public class NeutralFigureAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    private final NeutralFigure<FeaturePointer> figure;

    public NeutralFigureAction(NeutralFigure<FeaturePointer> figure, Set<FeaturePointer> options) {
        super(options);
        this.figure = figure;
    }

    public NeutralFigure<FeaturePointer> getFigure() {
        return figure;
    }
}
