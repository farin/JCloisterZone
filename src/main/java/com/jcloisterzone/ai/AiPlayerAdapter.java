package com.jcloisterzone.ai;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.CommitMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class AiPlayerAdapter {

    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    private final GameController gc;
    private final Player player;
    private final AiPlayer aiPlayer;

    private final int tilePlaceDelay;


    public AiPlayerAdapter(GameController gc, Player player, AiPlayer aiPlayer) {
        this.gc = gc;
        this.player = player;
        this.aiPlayer = aiPlayer;

        tilePlaceDelay = gc.getConfig().getAi().getPlace_tile_delay();
        aiPlayer.onGameStart(gc.getConfig(), gc.getGame().getSetup());
    }

    @Subscribe
    public void onGameStateChanged(GameChangedEvent ev) {
        GameState state = ev.getCurrentState();
        Player activePlayer = state.getActivePlayer();
        if (player.equals(activePlayer)) {
            executor.submit(() -> {
                WsInGameMessage msg = aiPlayer.apply(state);
                sendWithDelay(msg, msg instanceof CommitMessage ? 0 : tilePlaceDelay);
            }, "AI player " + player.getNick());
        }
    }

    private void sendWithDelay(WsMessage msg, int delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
        gc.getConnection().send(msg);
    }

}
