package com.jcloisterzone.wsio.server;

import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;

public class ServerRemoteClient extends RemoteClient {

	private String clientId;
	private String secret;

	public ServerRemoteClient(String sessionId, String name, ClientState state) {
		super(sessionId, name, state);
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
