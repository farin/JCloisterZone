package com.jcloisterzone.wsio.message;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.message.adapters.TokenAdapter;

@WsMessageCommand("PLACE_TOKEN")
public class PlaceTokenMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private long clock;
    private String parentId;

    @JsonAdapter(TokenAdapter.class)
    private Token token;
    private BoardPointer pointer;

    public PlaceTokenMessage() {
    }

    public PlaceTokenMessage(Token token, BoardPointer pointer) {
        this.token = token;
        this.pointer = pointer;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public long getClock() {
        return clock;
    }

    @Override
    public void setClock(long clock) {
        this.clock = clock;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
