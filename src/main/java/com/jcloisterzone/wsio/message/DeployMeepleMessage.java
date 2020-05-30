package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("DEPLOY_MEEPLE")
public class DeployMeepleMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private long clock;
    private String parentId;
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
