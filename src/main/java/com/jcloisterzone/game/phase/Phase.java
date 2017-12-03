package com.jcloisterzone.game.phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PayRansom;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;


public abstract class Phase {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final RandomGenerator random;

    //TODO change to class ? but what about skipping phase in handlePass
    private Phase defaultNext;

    public Phase(RandomGenerator random) {
        this.random = random;
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
        state = (new PayRansom(msg.getMeepleId())).apply(state);
        return promote(state);
    }

    public RandomGenerator getRandom() {
        return random;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
