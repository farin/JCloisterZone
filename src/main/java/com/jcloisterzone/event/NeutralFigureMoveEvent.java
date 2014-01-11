package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

public class NeutralFigureMoveEvent extends PlayEvent {

    public static final int DRAGON = 1;
    public static final int FAIRY = 2;

    public NeutralFigureMoveEvent(int type, Player player, Position position) {
        super(type, player, position);
    }

}
