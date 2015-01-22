package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GAME_UPDATE")
public class GameUpdateMessage implements WsInChannelMessage {

	private String channel;
	private GameMessage game;

	@Override
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public GameMessage getGame() {
		return game;
	}

	public void setGame(GameMessage game) {
		this.game = game;
	}
}
