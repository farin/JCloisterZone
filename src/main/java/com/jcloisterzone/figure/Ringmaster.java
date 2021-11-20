package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Acrobats;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class Ringmaster extends SmallFollower {

    private static final long serialVersionUID = 1L;

    public Ringmaster(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        if (feature instanceof Acrobats) {
            return new DeploymentCheckResult("Cannot place ringmaster on the acrobats space.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }
}
