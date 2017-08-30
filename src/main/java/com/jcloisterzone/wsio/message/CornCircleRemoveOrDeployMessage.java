package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CIRCLE_REMOVE_OR_DEPLOY")
public class CornCircleRemoveOrDeployMessage implements WsInGameMessage, WsReplayableMessage {

    public enum CornCicleOption { DEPLOY, REMOVE }

    private String gameId;
    private CornCicleOption value;

    public CornCircleRemoveOrDeployMessage() {
    }

    public CornCircleRemoveOrDeployMessage(String gameId, CornCicleOption value) {
        this.gameId = gameId;
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

    public CornCicleOption getValue() {
        return value;
    }

    public void setValue(CornCicleOption value) {
        this.value = value;
    }


}
