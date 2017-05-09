package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;

@Immutable
public class Dragon extends NeutralFigure<Position> {

    private static final long serialVersionUID = 1L;

    public Dragon(String id) {
        super(id);
    }

}
