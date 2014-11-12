package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CREATE_GAME")
public class CreateGameMessage implements WsMessage, WsInChannelMessage {

	private String name;
	private String channel;

	public CreateGameMessage(String name, String channel) {
		this.name = name;
		this.channel = channel;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
