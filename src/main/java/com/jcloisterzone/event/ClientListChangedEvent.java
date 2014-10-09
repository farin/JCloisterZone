package com.jcloisterzone.event;

import java.util.Arrays;

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

    @Override
    public String toString() {
        return super.toString() + " " + Arrays.toString(clients);
    }
}
