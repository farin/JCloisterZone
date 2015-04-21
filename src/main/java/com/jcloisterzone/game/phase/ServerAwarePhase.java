package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.RmiProxy;
import com.jcloisterzone.wsio.message.ToggleClockMessage;

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

    public Config getConfig() {
        return gc == null ? null : gc.getConfig();
    }

    public DebugConfig getDebugConfig() {
        Config config = getConfig();
        return config == null ? null : config.getDebug();
    }

    public boolean isLocalPlayer(Player player) {
        return player.getSlot().isOwn();
    }

    public void toggleClock(Player player) {
        if (isLocalPlayer(player)) {
            getConnection().send(new ToggleClockMessage(game.getGameId(), player.getIndex()));
        }
    }
}
