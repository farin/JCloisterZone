package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;

public class BuilderCapability extends Capability<BuilderState> {

    private static final long serialVersionUID = 1L;

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, BuilderState.UNUSED);
    }

    public GameState useBuilder(GameState state) {
        return updateModel(state, builderState -> builderState == BuilderState.UNUSED ? BuilderState.USED : builderState);
    }

    @Override
    public GameState onTurnPartCleanUp(GameState state) {
        return updateModel(state, bs -> {
            if (bs == BuilderState.USED) {
                return BuilderState.SECOND_TURN;
            }
            if (bs == BuilderState.SECOND_TURN) {
                return BuilderState.UNUSED;
            }
            return bs;
        });
    }
}
