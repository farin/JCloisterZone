package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.pointer.FeaturePointer;

@Immutable
public class Count extends NeutralFigure<FeaturePointer> {

    private static final long serialVersionUID = 1L;

    public Count(String id) {
        super(id);
    }

//    @Override
//    public void deploy(FeaturePointer at) {
//        if (at != null && !at.getLocation().isCityOfCarcassonneQuarter()) {
//            throw new IllegalArgumentException("Must be deployed on Quarter");
//        }
//        super.deploy(at);
//    }

}
