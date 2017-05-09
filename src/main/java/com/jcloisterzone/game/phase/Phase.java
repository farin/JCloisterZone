package com.jcloisterzone.game.phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PayRansom;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;


public abstract class Phase {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Game game;
    protected final GameController gc;

    //TODO change to class ? but what about skipping phase in handlePass
    private Phase defaultNext;

    public Phase(GameController gc) {
        this.gc = gc;
        this.game = gc.getGame();
    }

    public Phase getDefaultNext() {
        return defaultNext;
    }

    public void setDefaultNext(Phase defaultNext) {
        this.defaultNext = defaultNext;
    }

    public StepResult next(GameState state) {
        return new StepResult(state, defaultNext.getClass());
    }

    public StepResult next(GameState state, Class<? extends Phase> phaseClass) {
        return new StepResult(state, phaseClass);
        //next(state, game.getPhases().get(phaseClass));
    }

    public void next(GameState state, Phase phase) {
        phase.enter(state);
    }

    public abstract StepResult enter(GameState state);

    protected StepResult promote(GameState state) {
        return new StepResult(state.setPhase(getClass()), null);
    }

    protected GameState clearActions(GameState state) {
        return state.setPlayerActions(null);
    }

    @PhaseMessageHandler
    public StepResult handlePass(GameState state, PassMessage msg) {
        if (!state.getPlayerActions().isPassAllowed()) {
            throw new IllegalStateException("Pass is not allowed");
        }

        state = clearActions(state);
        if (getDefaultNext() instanceof PhantomPhase) {
            //skip PhantomPhase if user pass turn
            return getDefaultNext().next(state);
        } else {
            return next(state);
        }
    }

    @PhaseMessageHandler
    public StepResult handlePayRansom(GameState state, PayRansomMessage msg) {
        game.markUndo();
        state = (new PayRansom(msg.getMeepleId())).apply(state);
        return promote(state);
    }

    public DebugConfig getDebugConfig() {
        Config config = gc.getConfig();
        return config == null ? null : config.getDebug();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
