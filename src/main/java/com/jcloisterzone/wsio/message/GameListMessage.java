package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GAME_LIST")
public class GameListMessage implements WsInChannelMessage {

	private String channel;
	private GameMessage[] games;

	@Override
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public GameMessage[] getGames() {
		return games;
	}
	public void setGames(GameMessage[] games) {
		this.games = games;
	}

}
