package com.jcloisterzone.ai;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.CommitMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class AiPlayerAdapter {

    private final GameController gc;
    private final Player player;
    private final AiPlayer aiPlayer;


    public AiPlayerAdapter(GameController gc, Player player, AiPlayer aiPlayer) {
        this.gc = gc;
        this.player = player;
        this.aiPlayer = aiPlayer;
    }

    @Subscribe
    public void onGameStateChanged(GameChangedEvent ev) {
        GameState state = ev.getCurrentState();
        Player activePlayer = state.getActivePlayer();
        if (player.equals(activePlayer)) {
            WsInGameMessage msg = aiPlayer.apply(state);
            int delay = gc.getConfig().getAi_place_tile_delay();
            if (msg instanceof CommitMessage) {
                delay = 0;
            }
            sendWithDelay(msg, delay);
        }
    }

    private void sendWithDelay(WsMessage msg, int delay) {
        if (delay == 0) {
            gc.getConnection().send(msg);
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
            gc.getConnection().send(msg);
        }).start();
    }

}
