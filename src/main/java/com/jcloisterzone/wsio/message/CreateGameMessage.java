package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CREATE_GAME")
public class CreateGameMessage implements WsMessage, WsInChannelMessage {

	private String channel;

	public CreateGameMessage(String channel) {
		this.channel = channel;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
