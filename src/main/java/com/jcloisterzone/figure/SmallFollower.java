package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Garden;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class SmallFollower extends Follower {

    private static final long serialVersionUID = 1L;

    public SmallFollower(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (feature instanceof Garden) {
            return new DeploymentCheckResult("Cannot place small follower on the garden.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }

}
