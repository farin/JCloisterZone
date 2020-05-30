package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CIRCLE_REMOVE_OR_DEPLOY")
public class CornCircleRemoveOrDeployMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    public enum CornCircleOption { DEPLOY, REMOVE }

    private String gameId;
    private long clock;
    private String parentId;
    private CornCircleOption value;

    public CornCircleRemoveOrDeployMessage() {
    }

    public CornCircleRemoveOrDeployMessage(CornCircleOption value) {
        this.value = value;
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

    public CornCircleOption getValue() {
        return value;
    }

    public void setValue(CornCircleOption value) {
        this.value = value;
    }
}
