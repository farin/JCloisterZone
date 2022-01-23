package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.game.capability.RussianPromosTrapCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.CommitMessage;
import com.jcloisterzone.random.RandomGenerator;
import io.vavr.collection.List;

public class RussianPromosTrapPhase extends Phase {

    public RussianPromosTrapPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        RussianPromosTrapCapability russianPromos = state.getCapabilities().get(RussianPromosTrapCapability.class);
        List<RussianPromosTrapCapability.ExposedFollower> exposed = russianPromos.findExposedFollowers(state);
        Player player = state.getTurnPlayer();

        if (!exposed.filter(exp -> exp.getFollower().getPlayer() == player).isEmpty()) {
            // own follower is exposed, confirm prev action
            state = state.setPlayerActions(
                    new ActionsState(player, new ConfirmAction(), false)
            );
            return promote(state);
        }

        if (!exposed.isEmpty()) {
            state = russianPromos.trapFollowers(state, exposed);
        }

        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handleCommit(GameState state, CommitMessage msg) {
        RussianPromosTrapCapability russianPromos = state.getCapabilities().get(RussianPromosTrapCapability.class);
        state = russianPromos.trapFollowers(state);
        return next(state);
    }
}
