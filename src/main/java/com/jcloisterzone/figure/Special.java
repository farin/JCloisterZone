package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.FlyingMachine;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;

public abstract class Special extends Meeple {

    private static final long serialVersionUID = 1L;

    public Special(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (fp.getLocation() == Location.MONASTERY_AS_ABBOT || fp.getFeature().equals(FlyingMachine.class)) {
            return new DeploymentCheckResult("only follower is allowed");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }

}
