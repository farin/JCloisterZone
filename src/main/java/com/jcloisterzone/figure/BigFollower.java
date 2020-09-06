package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Garden;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class BigFollower extends Follower {

    private static final long serialVersionUID = 1L;

    public BigFollower(String id, Player player) {
        super(id, player);
    }

    @Override
    public int getPower(GameState state, Scoreable feature) {
        return 2;
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (feature instanceof Garden) {
            return new DeploymentCheckResult("Cannot place big follower on the garden.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }

}
