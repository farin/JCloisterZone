package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.wsio.Connection;

public class ServerAwarePhase extends Phase {

    @Deprecated //use connection only
    private final Client2ClientIF server;
    private final Connection conn;

    public ServerAwarePhase(Game game, Client2ClientIF server, Connection conn) {
        super(game);
        this.server = server;
        this.conn = conn;
    }

    public Client2ClientIF getServer() {
        return server;
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean isLocalPlayer(Player player) {
        return player.getSlot().isOwn();
    }
}
