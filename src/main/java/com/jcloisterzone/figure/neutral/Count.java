package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class Count extends NeutralFigure<FeaturePointer> {

    private static final long serialVersionUID = 1L;

    public Count(String id) {
        super(id);
    }
}
