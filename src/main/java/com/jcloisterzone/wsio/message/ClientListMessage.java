package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.server.RemoteClient;

@WsMessageCommand("CLIENT_LIST")
public class ClientListMessage implements WsMessage {

    private String gameId;
    private RemoteClient[] clients;

    public ClientListMessage(String gameId, RemoteClient[] clients) {
        super();
        this.gameId = gameId;
        this.clients = clients;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public RemoteClient[] getClients() {
        return clients;
    }

    public void setClients(RemoteClient[] clients) {
        this.clients = clients;
    }


}
