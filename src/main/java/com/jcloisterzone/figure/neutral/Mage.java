package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.pointer.FeaturePointer;

@Immutable
public class Mage extends NeutralFigure<FeaturePointer> {

    private static final long serialVersionUID = 1L;

    public Mage(String id) {
        super(id);
    }
}
