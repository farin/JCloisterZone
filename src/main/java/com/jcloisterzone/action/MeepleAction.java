package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;

import io.vavr.collection.Set;

public class MeepleAction extends SelectFeatureAction {

    private static final long serialVersionUID = 1L;

    private final Class<? extends Meeple> meepleType;

    public MeepleAction(Class<? extends Meeple> meepleType, Set<FeaturePointer> options) {
        super(options);
        this.meepleType = meepleType;
    }

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    @Override
    public void perform(GameController gc, FeaturePointer fp) {
        GameState state = gc.getGame().getState();
        String meepleId = state.getActivePlayer().getMeepleFromSupply(state, meepleType).getId();
        gc.getConnection().send(
            new DeployMeepleMessage(gc.getGame().getGameId(), fp, meepleId));

    }

    @Override
    public String toString() {
        return "place " + meepleType.getSimpleName();
    }
}
