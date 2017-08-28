package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.CommitMessage;

public class CommitActionPhase extends Phase {

    public CommitActionPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();
        state = state.setPlayerActions(
            new ActionsState(player, new ConfirmAction(), false)
        );
        return promote(state);
    }

    @PhaseMessageHandler
    public StepResult handleCommit(GameState state, CommitMessage msg) {
        state = clearActions(state);
        return next(state);
    }
}
