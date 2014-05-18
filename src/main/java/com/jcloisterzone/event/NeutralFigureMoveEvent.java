package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

public class NeutralFigureMoveEvent extends PlayEvent {

    public static final int DRAGON = 1;
    public static final int FAIRY = 2;

    private final Position fromPosition;

    public NeutralFigureMoveEvent(int type, Player player, Position fromPosition, Position position) {
        super(type, player, position);
        this.fromPosition = fromPosition;
    }

    public Position getFromPosition() {
        return fromPosition;
    }

}
