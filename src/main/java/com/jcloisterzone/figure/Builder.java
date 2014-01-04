package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.IsOccupoedAndUncompleted;
import com.jcloisterzone.game.Game;

public class Builder extends Special {

    private static final long serialVersionUID = 1189566966196473830L;

    public Builder(Game game, Player player) {
        super(game, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(Feature f) {
        if (!(f instanceof City || f instanceof Road) ) {
            return new DeploymentCheckResult("Builder must be placed in city or on road only.");
        }
        if (!f.walk(new IsOccupoedAndUncompleted().with(Follower.class))) {
            return new DeploymentCheckResult("Feature is not occupied by follower or completed.");
        }
        return super.isDeploymentAllowed(f);
    }

}
