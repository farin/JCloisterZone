package com.jcloisterzone.ai;

import java.lang.reflect.Proxy;

import com.jcloisterzone.Player;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.rmi.mina.ClientStub;

public abstract class AiPlayer implements UserInterface {

	private Game game;
	private ServerIF server;
	private Player player;

	public void setGame(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	public ServerIF getServer() {
		return server;
	}

	public void setServer(ServerIF server) {
		this.server = server;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	protected Board getBoard() {
		return game.getBoard();
	}

	protected TilePack getTilePack() {
		return game.getTilePack();
	}

	protected ClientStub getClientStub() {
		return (ClientStub) Proxy.getInvocationHandler(server);
	}

	protected boolean isMe(Player p) {
		//nestaci porovnavat ref ?
		return p.getIndex() == player.getIndex();
	}

	public boolean isAiPlayerActive() {
		if (server == null) return false;
		Player activePlayer = game.getActivePlayer();
		if (activePlayer.getIndex() != player.getIndex()) return false;
		return getClientStub().isLocalPlayer(activePlayer);
	}
	
	@Override
	public void showWarning(String title, String message) {
		//do nothing
	}



}
