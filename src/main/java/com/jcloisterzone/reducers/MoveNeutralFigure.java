package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.event.play.NeutralFigureMoved;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;

public class MoveNeutralFigure<P extends BoardPointer> implements Reducer {

    private final Player triggeringPlayer;
    private final NeutralFigure<P> figure;
    private final P pointer;

    public MoveNeutralFigure(NeutralFigure<P> figure, P pointer) {
        this(figure, pointer, null);
    }

    public MoveNeutralFigure(NeutralFigure<P> figure, P pointer, Player triggeringPlayer) {
        this.figure = figure;
        this.pointer = pointer;
        this.triggeringPlayer = triggeringPlayer;
    }

    @Override
    public GameState apply(GameState state) {
        NeutralFiguresState nfState = state.getNeutralFigures();
        BoardPointer from = nfState.getDeployedNeutralFigures().get(figure).getOrNull();

        if (pointer == null) {
            nfState = nfState.setDeployedNeutralFigures(
                nfState.getDeployedNeutralFigures().remove(figure)
            );
        } else {
            nfState = nfState.setDeployedNeutralFigures(
                nfState.getDeployedNeutralFigures().put(figure, pointer)
            );
        }

        state = state.setNeutralFigures(nfState);
        state = state.appendEvent(
            new NeutralFigureMoved(PlayEventMeta.createWithPlayer(triggeringPlayer), figure, from, pointer)
        );
        return state;
    }

}
