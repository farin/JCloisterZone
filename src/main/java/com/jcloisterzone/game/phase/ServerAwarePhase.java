package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.RmiProxy;

public class ServerAwarePhase extends Phase {

    private final GameController gc;

    public ServerAwarePhase(Game game, GameController gc) {
        super(game);
        this.gc = gc;
    }

    public RmiProxy getServer() {
        return gc.getRmiProxy();
    }

    public Connection getConnection() {
        return gc.getConnection();
    }

    public GameController getGameController() {
        return gc;
    }

    public boolean isLocalPlayer(Player player) {
        return player.getSlot().isOwn();
    }
}
