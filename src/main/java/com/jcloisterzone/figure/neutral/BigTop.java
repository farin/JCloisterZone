package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class BigTop extends NeutralFigure<Position> {

    private static final long serialVersionUID = 1L;

    public BigTop(String id) {
        super(id);
    }
}
