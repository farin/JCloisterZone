package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.SetNextPlayer;
import io.vavr.collection.HashSet;
import io.vavr.collection.Queue;

/**
 * real end of turn and switch to next player
 */
public class CleanUpTurnPhase extends Phase {

    public CleanUpTurnPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onTurnCleanUp(state);
        }

        if (!state.getFlags().isEmpty()) {
            state = state.setFlags(HashSet.empty());
        }

        BazaarCapabilityModel bazaarModel = state.getCapabilityModel(BazaarCapability.class);
        Queue<BazaarItem> bazaarSupply = bazaarModel == null ? null : bazaarModel.getSupply();
        TilePack tilePack = state.getTilePack();
        if (tilePack.isEmpty() && bazaarSupply == null) {
            // TODO allow placing Abbey if enabled by custom rule (TODO add it, default is don't allow)
            // but don't add fairy points during this special turns
            return next(state, state.getEndPhase());
        } else {
            state = (new SetNextPlayer()).apply(state);
            return next(state);
        }
    }
}
