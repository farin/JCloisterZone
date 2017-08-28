package com.jcloisterzone.event.play;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Token;

public class TokenPlacedEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final Token token;
    private final FeaturePointer pointer;

    public TokenPlacedEvent(PlayEventMeta metadata, Token token, FeaturePointer pointer) {
        super(metadata);
        this.token = token;
        this.pointer = pointer;
    }

    public Token getToken() {
        return token;
    }

    public FeaturePointer getPointer() {
        return pointer;
    }
}
