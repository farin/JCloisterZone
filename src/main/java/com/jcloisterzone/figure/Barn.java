package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Structure;
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
    public boolean canBeEatenByBlackDragon(GameState state) {
        return false;
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (!(feature instanceof Field)) {
            return new DeploymentCheckResult("The barn must be placed only on a field.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }
}
