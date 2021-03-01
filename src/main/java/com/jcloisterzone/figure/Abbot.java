package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.game.state.GameState;

public class Abbot extends Follower {

    private static final long serialVersionUID = 1L;

    public Abbot(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (!(fp.getLocation() == Location.QUARTER_CATHEDRAL ||
                feature instanceof Cloister || feature instanceof Garden || feature instanceof FlyingMachine|| feature instanceof TrapFeature)) {
            return new DeploymentCheckResult("Abbot must be placed only at cloister or garden.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }

}
