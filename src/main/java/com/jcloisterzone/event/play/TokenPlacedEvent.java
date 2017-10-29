package com.jcloisterzone.event.play;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.game.Token;

public class TokenPlacedEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final Token token;
    private final BoardPointer pointer;

    public TokenPlacedEvent(PlayEventMeta metadata, Token token, BoardPointer pointer) {
        super(metadata);
        this.token = token;
        this.pointer = pointer;
    }

    public Token getToken() {
        return token;
    }

    public BoardPointer getPointer() {
        return pointer;
    }
}
