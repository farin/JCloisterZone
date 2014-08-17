package com.jcloisterzone.event;

import com.jcloisterzone.wsio.server.RemoteClient;

public class ClientListChangedEvent extends Event {

    private RemoteClient[] clients;

    public ClientListChangedEvent(RemoteClient[] clients) {
        super();
        this.clients = clients;
    }

    public RemoteClient[] getClients() {
        return clients;
    }

    public void setClients(RemoteClient[] clients) {
        this.clients = clients;
    }
}
