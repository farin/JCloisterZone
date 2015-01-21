package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CLIENT_UPDATE")
public class ClientUpdateMessage implements WsInGameMessage, WsInChannelMessage {

	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_IN_GAME = "IN_GAME";
	public static final String STATUS_OFFLINE = "OFFLINE";

	private String gameId;
	private String channel;
	private String sessionId;
	private String name;
	private String status;

	public ClientUpdateMessage() {
	}

	public ClientUpdateMessage(String gameId, String sessionId, String name, String status) {
		this.gameId = gameId;
		this.sessionId = sessionId;
		this.name = name;
		this.status = status;
	}

	@Override
	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


}
