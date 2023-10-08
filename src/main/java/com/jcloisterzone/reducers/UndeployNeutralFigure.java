package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;

public class UndeployNeutralFigure extends AbstractUndeploy {

    private final NeutralFigure figure;
    /** true if meep le is returned different way than scoring feature */
    private final boolean forced;

    public UndeployNeutralFigure(NeutralFigure figure, boolean forced) {
        this.figure = figure;
        this.forced = forced;
    }

    @Override
    public GameState apply(GameState state) {
        BoardPointer source = figure.getDeployment(state);

        PlayEventMeta metaWithPlayer = PlayEventMeta.createWithActivePlayer(state);
        state = undeploy(state, metaWithPlayer, figure, source, forced, state.getActivePlayer());

        return state;
    }

    public boolean isForced() {
        return forced;
    }
}
