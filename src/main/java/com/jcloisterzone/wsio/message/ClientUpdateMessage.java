package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CLIENT_UPDATE")
public class ClientUpdateMessage implements WsInGameMessage, WsInChannelMessage {

    public enum ClientState {
        ACTIVE, IN_GAME, OFFLINE
    }

    private String gameId;
    private String channel;
    private String sessionId;
    private String name;
    private ClientState state;

    public ClientUpdateMessage() {
    }

    public ClientUpdateMessage(String sessionId, String name, ClientState state) {
        this.sessionId = sessionId;
        this.name = name;
        this.state = state;
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
    public String getChannel() {
        return channel;
    }

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        this.state = state;
    }
}
