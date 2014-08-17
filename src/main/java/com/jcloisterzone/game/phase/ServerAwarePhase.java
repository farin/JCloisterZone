package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.RmiProxy;
import com.jcloisterzone.wsio.Connection;

public class ServerAwarePhase extends Phase {

    private final Connection conn;

    public ServerAwarePhase(Game game, Connection conn) {
        super(game);
        this.conn = conn;
    }

    public RmiProxy getServer() {
        return conn.getRmiProxy();
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean isLocalPlayer(Player player) {
        return player.getSlot().isOwn();
    }
}
