package com.jcloisterzone.game.state.mixins;

import java.util.function.Function;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlayersState;

public interface PlayersMixin extends ActionsMixin {

    PlayersState getPlayers();
    GameState setPlayers(PlayersState players);

    default Player getTurnPlayer() {
        return getPlayers().getTurnPlayer();
    }

    default Player getActivePlayer() {
        ActionsState as = getPlayerActions();
        return as == null ? null : as.getPlayer();
    }

    default GameState mapPlayers(Function<PlayersState, PlayersState> fn) {
        return setPlayers(fn.apply(getPlayers()));
    }

}
