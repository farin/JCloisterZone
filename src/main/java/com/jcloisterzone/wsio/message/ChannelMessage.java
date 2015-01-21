package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.server.RemoteClient;

@WsMessageCommand("CHANNEL")
public class ChannelMessage implements WsMessage {

    private String name;
    private RemoteClient[] clients;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public RemoteClient[] getClients() {
		return clients;
	}

	public void setClients(RemoteClient[] clients) {
		this.clients = clients;
	}
}
