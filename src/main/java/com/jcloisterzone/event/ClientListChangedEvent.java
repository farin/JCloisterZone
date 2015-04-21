package com.jcloisterzone.event;

import java.util.Arrays;
import java.util.List;

import com.jcloisterzone.wsio.server.RemoteClient;

public class ClientListChangedEvent extends Event {

    private List<RemoteClient> clients;

    public ClientListChangedEvent(List<RemoteClient> clients) {
        super();
        this.clients = clients;
    }

    public List<RemoteClient> getClients() {
        return clients;
    }

    public void setClients(List<RemoteClient> clients) {
        this.clients = clients;
    }

    @Override
    public String toString() {
        return super.toString() + " " + Arrays.toString(clients.toArray());
    }
}
