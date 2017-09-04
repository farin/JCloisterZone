package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.pointer.FeaturePointer;

@Immutable
public class Count extends NeutralFigure<FeaturePointer> {

    private static final long serialVersionUID = 1L;

    public Count(String id) {
        super(id);
    }
}
