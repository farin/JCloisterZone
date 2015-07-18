package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.RemoveLonelyBuilderAndPig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.PigCapability;

public abstract class Follower extends Meeple {

    private static final long serialVersionUID = -659337195197201811L;

    private boolean inPrison;

    public Follower(Game game, Integer idSuffix, Player player) {
        super(game, idSuffix, player);
    }

    public int getPower() {
        return 1;
    }

    @Override
    public boolean canBeEatenByDragon() {
        return !(getFeature() instanceof Castle);
    }

    public boolean isInPrison() {
        return inPrison;
    }

    public void setInPrison(boolean inPrison) {
        this.inPrison = inPrison;
        if (inPrison) {
            setFeaturePointer(null);
        }
    }

    @Override
    public boolean isInSupply() {
        return !inPrison && super.isInSupply();
    }

    @Override
    public void setFeaturePointer(FeaturePointer featurePointer) {
        if (featurePointer != null && inPrison) {
            inPrison = false;
        }
        super.setFeaturePointer(featurePointer);
    }


    //TODO ??? can be this in score visitor instead of here ???
    @Override
    public void undeploy(boolean checkForLonelyBuilderOrPig) {
        assert !isInPrison();
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

    @Override
    public String toString() {
        return super.toString() + (inPrison ? "(PRISON)" : "");
    }
}
