package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.io.message.DeployMeepleMessage;

import java.util.function.Function;

public class CocFinalScoringPhase extends AbstractCocScoringPhase {

    public CocFinalScoringPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    protected boolean isLast(GameState state, Player player, boolean actionUsed) {
        Player lastNoPass = state.getCapabilityModel(CountCapability.class).getFinalScoringLastMeepleDeployPlayer();
        if (lastNoPass == null) {
            // no player has meeple (or everybody pass) without deploying any meeple
            return state.getTurnPlayer().equals(player);
        }
        return lastNoPass.equals(player) && !actionUsed;
    }

    @Override
    protected Function<Feature, Boolean> getAllowedFeaturesFilter(GameState state) {
        return f -> {
            if (f instanceof Farm) {
                return true;
            }
            if (f instanceof Completable) {
                return !((Completable)f).isCompleted(state);
            }
            throw new UnsupportedOperationException();
        };
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Player player = state.getActivePlayer();
        Follower follower = player.getFollowers(state).find(f -> f.getId().equals(msg.getMeepleId())).get();

        state = state.mapCapabilityModel(CountCapability.class, m -> m.setFinalScoringLastMeepleDeployPlayer(player));
        state = (new DeployMeeple(follower, fp)).apply(state);
        return nextPlayer(state, player, true);
    }
}
