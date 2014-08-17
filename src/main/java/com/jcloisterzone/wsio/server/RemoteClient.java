package com.jcloisterzone.wsio.server;

public class RemoteClient {
    private String clientId, name;

    public RemoteClient(String clientId, String name) {
        super();
        this.clientId = clientId;
        this.name = name;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}