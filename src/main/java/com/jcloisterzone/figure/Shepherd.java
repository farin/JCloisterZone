package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;

public class Shepherd extends Special {

	private static final long serialVersionUID = 1L;

    public Shepherd(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (!(feature instanceof Farm)) {
            return new DeploymentCheckResult("Shepherd must be placed on a farm only.");
        }
        if (feature.getSpecialMeeples(state).filter(f -> f instanceof Shepherd).nonEmpty()) {
            return new DeploymentCheckResult("Farm is already occupied by Shepherd.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }

}
