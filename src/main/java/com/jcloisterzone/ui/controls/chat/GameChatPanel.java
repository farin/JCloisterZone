package com.jcloisterzone.ui.controls.chat;

import java.awt.Color;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.wsio.message.PostChatMessage;

public class GameChatPanel extends ChatPanel {

    private final Game game;

    public GameChatPanel(Client client, Game game) {
        super(client);
        this.game = game;
    }


    @Override
    protected PostChatMessage createPostChatMessage(String msg) {
        PostChatMessage pcm = new PostChatMessage(msg);
        pcm.setGameId(game.getGameId());
        return pcm;
    }

    /**
     * More then one client can play from one seat. Find local player, prefer
     * active, than human player, latest is AI.
     *
     * @return
     */
    @Override
    protected ReceivedChatMessage createReceivedMessage(ChatEvent ev) {
        String nick = ev.getRemoteClient().getName();
        Color color = client.getTheme().getChatNeutralColor();
        GameState state = game.getState();

        if (game.isStarted()) {
            Player selected = null, active = state.getActivePlayer();
            for (Player player : state.getPlayers().getPlayers()) {
                boolean isAi = player.getSlot().getAiClassName() != null;
                if (player.getSlot().getSessionId().equals(ev.getRemoteClient().getSessionId())) {
                    if (selected == null) {
                        selected = player;
                    } else {
                        if (selected.getSlot().getAiClassName() != null && !isAi) {
                            // prefer real user for remote inactive client with
                            // more players
                            selected = player;
                        }
                    }
                    if (player.equals(active) && !isAi) {
                        selected = player;
                        break;
                    }
                }
            }
            if (selected != null) {
                nick = selected.getNick();
                color = selected.getColors().getFontColor();
            }
        } else {
             PlayerSlot[] slots = game.getPlayerSlots();
             for (PlayerSlot slot: slots) {
                 if (! ev.getRemoteClient().getSessionId().equals(slot.getSessionId())) continue;
                 if (!slot.isAi() && !slot.getNickname().equals("")) {
                     nick = slot.getNickname();
                     color = slot.getColors().getFontColor();
                     break;
                 }
             }
        }
        return new ReceivedChatMessage(ev, nick, color);
    }

}
