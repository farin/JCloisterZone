package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.state.GameState;

public class Barn extends Special {

    private static final long serialVersionUID = -1422237898274679967L;

    public Barn(String id, Player player) {
        super(id, player);
    }

    @Override
    public boolean canBeEatenByDragon(GameState state) {
        return false;
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Feature feature) {
        if (!(feature instanceof Farm)) {
            return new DeploymentCheckResult("The barn must be placed only on a farm.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }
}
