package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.server.Connection;

@WsMessageCommand("CLIENT_LIST")
public class ClientListMessage implements WsMessage {

    private String gameId;
    private Connection[] clients;

    public ClientListMessage(String gameId, Connection[] clients) {
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

    public Connection[] getClients() {
        return clients;
    }

    public void setClients(Connection[] clients) {
        this.clients = clients;
    }


}
