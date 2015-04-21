package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.game.Game;

public class Pig extends Special {

    private static final long serialVersionUID = -6315956811639409025L;

    public Pig(Game game, Player player) {
        super(game, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(Feature farm) {
        if (!(farm instanceof Farm)) {
            return new DeploymentCheckResult("Pig must be placed on a farm only.");
        }
        if (!farm.walk(new IsOccupied().with(Follower.class))) {
            return new DeploymentCheckResult("Feature is not occupied by follower.");
        }
        return super.isDeploymentAllowed(farm);
    }
}
