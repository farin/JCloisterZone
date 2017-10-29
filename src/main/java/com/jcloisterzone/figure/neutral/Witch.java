package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.pointer.FeaturePointer;

@Immutable
public class Witch extends NeutralFigure<FeaturePointer> {

    private static final long serialVersionUID = 1L;

    public Witch(String id) {
        super(id);
    }
}
