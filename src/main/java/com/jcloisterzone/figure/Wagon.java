package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.state.GameState;

public class Wagon extends Follower {

    private static final long serialVersionUID = 1L;

    public Wagon(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Feature feature) {
        if (feature instanceof Tower) {
            return new DeploymentCheckResult("Cannot place wagon on the tower.");
        }
        if (feature instanceof Farm) {
            return new DeploymentCheckResult("Cannot place wagon on the farm.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }
}
