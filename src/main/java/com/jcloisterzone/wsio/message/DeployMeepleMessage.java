package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("DEPLOY_MEEPLE")
public class DeployMeepleMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private FeaturePointer pointer;
    private String meepleId;

    public DeployMeepleMessage() {
    }

    public DeployMeepleMessage(FeaturePointer pointer, String meepleId) {
        this.pointer = pointer;
        this.meepleId = meepleId;
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

    public String getMeepleId() {
        return meepleId;
    }

    public void setMeepleId(String meepleId) {
        this.meepleId = meepleId;
    }
}
