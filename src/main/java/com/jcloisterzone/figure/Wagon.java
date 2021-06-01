package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Garden;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.state.GameState;

public class Wagon extends Follower {

    private static final long serialVersionUID = 1L;

    public Wagon(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (fp.getLocation() == Location.QUARTER_MARKET) {
            return new DeploymentCheckResult("Cannot place wagon on the market quarter.");
        }
        if (feature instanceof Tower) {
            return new DeploymentCheckResult("Cannot place wagon on the tower.");
        }
        if (feature instanceof Field) {
            return new DeploymentCheckResult("Cannot place wagon on the field.");
        }
        if (feature instanceof Garden) {
            return new DeploymentCheckResult("Cannot place wagon on the garden.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }
}
