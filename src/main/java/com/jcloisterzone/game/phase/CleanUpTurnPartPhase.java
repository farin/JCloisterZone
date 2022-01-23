package com.jcloisterzone.game.phase;

import com.jcloisterzone.event.DoubleTurnEvent;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;

/**
 *  End of turn part. For builder double repeat turn otherwise proceed to real end of turn.
 */
public class CleanUpTurnPartPhase extends Phase {

    private Phase secondPartStartPhase;

    public CleanUpTurnPartPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    public void setSecondPartStartPhase(Phase secondPartStartPhase) {
        this.secondPartStartPhase = secondPartStartPhase;
    }

    @Override
    public StepResult enter(GameState state) {
        BuilderState builderState = state.getCapabilityModel(BuilderCapability.class);
        boolean builderTakeAnotherTurn = builderState == BuilderState.USED;

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onTurnPartCleanUp(state);
        }

        if (!state.getFlags().isEmpty()) {
            state = state.setFlags(state.getFlags()
                .remove(Flag.PORTAL_USED)
                .remove(Flag.NO_PHANTOM)
                .remove(Flag.FLYING_MACHINE_USED)
            );
        }

        if (builderTakeAnotherTurn) {
        	state = state.appendEvent(
        		new DoubleTurnEvent(PlayEventMeta.createWithoutPlayer())
            );
            return next(state, secondPartStartPhase);
        } else {
            return next(state);
        }
    }
}
