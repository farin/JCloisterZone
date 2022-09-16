package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.event.NeutralFigureReturned;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;

public class ReturnNeutralFigure implements Reducer {

    private final Player triggeringPlayer;
    private final NeutralFigure<?> figure;

    public ReturnNeutralFigure(NeutralFigure<?> figure) {
        this(figure, null);
    }

    public ReturnNeutralFigure(NeutralFigure<?> figure,  Player triggeringPlayer) {
        this.figure = figure;
        this.triggeringPlayer = triggeringPlayer;
    }

    @Override
    public GameState apply(GameState state) {
        NeutralFiguresState nfState = state.getNeutralFigures();
        BoardPointer from = nfState.getDeployedNeutralFigures().get(figure).getOrNull();

        nfState = nfState.setDeployedNeutralFigures(
            nfState.getDeployedNeutralFigures().remove(figure)
        );
        state = state.setNeutralFigures(nfState);
        state = state.appendEvent(
            new NeutralFigureReturned(PlayEventMeta.createWithPlayer(triggeringPlayer), figure, from, true, triggeringPlayer)
        );
        return state;
    }

}
