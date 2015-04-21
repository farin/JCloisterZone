package com.jcloisterzone.event;

import com.jcloisterzone.wsio.server.RemoteClient;

public class ChatEvent extends Event {

    private final RemoteClient remoteClient;
    private final String text;

    public ChatEvent(RemoteClient remoteClient, String text) {
        this.remoteClient = remoteClient;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public RemoteClient getRemoteClient() {
        return remoteClient;
    }
}
