package com.jcloisterzone.game.phase;

import java.lang.reflect.Proxy;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.rmi.mina.ClientStub;

public class ServerAwarePhase extends Phase {

	private final ServerIF server;

	public ServerAwarePhase(Game game, ServerIF server) {
		super(game);
		this.server = server;
	}

	public ServerIF getServer() {
		return server;
	}

	public boolean isLocalPlayer(Player player) {
		return ((ClientStub)Proxy.getInvocationHandler(server)).isLocalPlayer(player);
	}

	public boolean isLocalSlot(PlayerSlot slot) {
		return ((ClientStub)Proxy.getInvocationHandler(server)).isLocalSlot(slot);
	}

}
