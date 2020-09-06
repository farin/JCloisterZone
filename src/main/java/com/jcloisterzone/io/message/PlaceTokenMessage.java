package com.jcloisterzone.io.message;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.io.MessageCommand;
import com.jcloisterzone.io.adapters.TokenAdapter;

@MessageCommand("PLACE_TOKEN")
public class PlaceTokenMessage extends AbstractMessage implements ReplayableMessage {

    @JsonAdapter(TokenAdapter.class)
    private Token token;
    private BoardPointer pointer;

    public PlaceTokenMessage() {
    }

    public PlaceTokenMessage(Token token, BoardPointer pointer) {
        this.token = token;
        this.pointer = pointer;
    }

    public BoardPointer getPointer() {
        return pointer;
    }

    public void setPointer(BoardPointer pointer) {
        this.pointer = pointer;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
