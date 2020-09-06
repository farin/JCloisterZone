package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import io.vavr.collection.Set;

public class NeutralFigureAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    private final NeutralFigure<FeaturePointer> figure;

    public NeutralFigureAction(NeutralFigure<FeaturePointer> figure, Set<FeaturePointer> options) {
        super(options);
        this.figure = figure;
    }

    @Override
    public Message select(FeaturePointer target) {
        return new MoveNeutralFigureMessage(figure.getId(), target);
    }

    public NeutralFigure<FeaturePointer> getFigure() {
        return figure;
    }

    @Override
    public String toString() {
        return "move " + figure;
    }
}
