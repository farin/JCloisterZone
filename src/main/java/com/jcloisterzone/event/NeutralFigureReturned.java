package com.jcloisterzone.event;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.Player;

public class NeutralFigureReturned extends PlayEvent {

    private final BoardPointer from;
    private final NeutralFigure<?> neutralFigure;
    private final boolean forced;
    /** true if meeple is returned different way than scoring feature */
    private final Player player;

    public NeutralFigureReturned(PlayEventMeta metadata, NeutralFigure<?> neutralFigure, BoardPointer from, Boolean forced, Player player) {
        super(metadata);
        this.neutralFigure = neutralFigure;
        this.from = from;
        this.forced = forced;
        this.player = player;
    }

    public BoardPointer getFrom() {
        return from;
    }

    public NeutralFigure<?> getNeutralFigure() {
        return neutralFigure;
    }

    public boolean isForced() {
        return forced;
    }

    public Player getPlayer() {
        return player;
    }
}
