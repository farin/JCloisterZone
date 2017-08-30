package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("PLACE_TOKEN")
public class PlaceTokenMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private Token token;
    private FeaturePointer pointer;

    public PlaceTokenMessage() {
    }

    public PlaceTokenMessage(Token token, FeaturePointer pointer) {
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

    public FeaturePointer getPointer() {
        return pointer;
    }

    public void setPointer(FeaturePointer pointer) {
        this.pointer = pointer;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
