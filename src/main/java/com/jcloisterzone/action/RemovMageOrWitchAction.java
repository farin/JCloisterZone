package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import io.vavr.collection.Set;

public class RemovMageOrWitchAction extends AbstractPlayerAction<NeutralFigure<FeaturePointer>> {

    public RemovMageOrWitchAction(Set<NeutralFigure<FeaturePointer>> options) {
        super(options);
    }

    @Override
    public Message select(NeutralFigure<FeaturePointer> option) {
        return new MoveNeutralFigureMessage(option.getId(), null);
    }

}
