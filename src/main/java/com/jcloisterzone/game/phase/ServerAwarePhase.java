package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.wsio.Connection;

public class ServerAwarePhase extends Phase {

    @Deprecated //use connection only
    private final ServerIF server;
    private final Connection conn;

    public ServerAwarePhase(Game game, ServerIF server, Connection conn) {
        super(game);
        this.server = server;
        this.conn = conn;
    }

    public ServerIF getServer() {
        return server;
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean isLocalPlayer(Player player) {
        return player.getSlot().isOwn();
    }
}
