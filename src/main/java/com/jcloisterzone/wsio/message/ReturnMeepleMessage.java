package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("RETURN_MEEPLE")
public class ReturnMeepleMessage implements WsInGameMessage, WsReplayableMessage {

    public enum ReturnMeepleSource {
        PRINCESS, SIEGE_ESCAPE, FESTIVAL, CORN_CIRCLE;
    }

    private String gameId;
    private MeeplePointer pointer;
    private ReturnMeepleSource source;


    public ReturnMeepleMessage(String gameId, MeeplePointer pointer, ReturnMeepleSource source) {
        super();
        this.gameId = gameId;
        this.pointer = pointer;
        this.source = source;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public MeeplePointer getPointer() {
        return pointer;
    }

    public void setPointer(MeeplePointer pointer) {
        this.pointer = pointer;
    }

    public ReturnMeepleSource getSource() {
        return source;
    }

    public void setSource(ReturnMeepleSource source) {
        this.source = source;
    }
}