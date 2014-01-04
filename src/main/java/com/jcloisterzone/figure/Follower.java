package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.feature.visitor.RemoveLonelyBuilderAndPig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.PigCapability;

public abstract class Follower extends Meeple {

    private static final long serialVersionUID = -659337195197201811L;

    public Follower(Game game, Player player) {
        super(game, player);
    }

    public int getPower() {
        return 1;
    }

    @Override
    public boolean canBeEatenByDragon() {
        return !(getFeature() instanceof Castle);
    }

    @Override
    public void deployUnoccupied(Tile tile, Location loc) {
        Feature feature = getDeploymentFeature(tile, loc);
        if (feature.walk(new IsOccupied())) {
            throw new IllegalArgumentException("Feature is occupied.");
        }
        deploy(tile, loc, feature);
    }


    //TODO ??? can be this in score visitor instead of here ???
    public void undeploy(boolean checkForLonelyBuilderOrPig) {
        //store ref which is lost be super call
        Feature piece = getFeature();
        super.undeploy(checkForLonelyBuilderOrPig); //clear piece
        if (checkForLonelyBuilderOrPig) {
            boolean builder = game.hasCapability(BuilderCapability.class) && (piece instanceof City || piece instanceof Road);
            boolean pig = game.hasCapability(PigCapability.class) && piece instanceof Farm;
            if (builder || pig) {
                Special toRemove = piece.walk(new RemoveLonelyBuilderAndPig(getPlayer()));
                if (toRemove != null) {
                    toRemove.undeploy(false);
                }
            }
        }
    }


}
