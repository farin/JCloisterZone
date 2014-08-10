package com.jcloisterzone.event;

import com.jcloisterzone.wsio.server.Connection;

public class ClientListChangedEvent extends Event {

    private Connection[] clients;

    public ClientListChangedEvent(Connection[] clients) {
        super();
        this.clients = clients;
    }

    public Connection[] getClients() {
        return clients;
    }

    public void setClients(Connection[] clients) {
        this.clients = clients;
    }
}
