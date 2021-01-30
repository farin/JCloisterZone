package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.game.state.GameState;

public class SetNextPlayer implements Reducer {
    private Player player;

    public SetNextPlayer() {
    }

    public SetNextPlayer(Player player) {
        this.player = player;
    }

    @Override
    public GameState apply(GameState state) {
        Player p = player == null ? state.getTurnPlayer().getNextPlayer(state) : player;
        state = state.mapPlayers(ps -> ps.setTurnPlayerIndex(p.getIndex()));
        state = state.setTurnNumber(state.getTurnNumber() + 1);
        state = state.appendEvent(
            new PlayerTurnEvent(PlayEventMeta.createWithoutPlayer(), p)
        );
        return state;
    }

}
