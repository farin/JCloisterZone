package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.Game;

public class Wagon extends Follower {

    private static final long serialVersionUID = 2585914429763599776L;

    public Wagon(Game game, Player player) {
        super(game, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(Feature f) {
        if (f instanceof Tower) {
            return new DeploymentCheckResult("Cannot place wagon on the tower.");
        }
        if (f instanceof Farm) {
            return new DeploymentCheckResult("Cannot place wagon on the farm.");
        }
        return super.isDeploymentAllowed(f);
    }
}
