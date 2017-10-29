package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CIRCLE_REMOVE_OR_DEPLOY")
public class CornCircleRemoveOrDeployMessage implements WsInGameMessage, WsReplayableMessage {

    public enum CornCircleOption { DEPLOY, REMOVE }

    private String gameId;
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

    public CornCircleOption getValue() {
        return value;
    }

    public void setValue(CornCircleOption value) {
        this.value = value;
    }
}
