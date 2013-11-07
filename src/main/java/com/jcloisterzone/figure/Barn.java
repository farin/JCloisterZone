package com.jcloisterzone.figure;

import java.util.HashMap;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;

public class Barn extends Special {

    private static final long serialVersionUID = -1422237898274679967L;

    public Barn(Game game, Player player) {
        super(game, player);
    }

    @Override
    public boolean canBeEatenByDragon() {
        return false;
    }

    @Override
    protected void checkDeployment(Feature feature) {
        if (!(feature instanceof Farm)) {
            throw new IllegalArgumentException("The barn must be placed only on a farm.");
        }
        Farm farm = (Farm) feature;

        FarmScoreContext ctx = farm.getScoreContext();
        ctx.setCityCache(new HashMap<City, CityScoreContext>());
        farm.walk(ctx);

        if (!farm.getTile().getGame().hasRule(CustomRule.MULTI_BARN_ALLOWED)) {
            for (Special m : ctx.getSpecialMeeples()) {
                if (m instanceof Barn) {
                    throw new IllegalArgumentException("Another barn is already placed on the farm.");
                }
            }
        }

        //all ok - score non barn meeples
        for (Player owner : ctx.getMajorOwners()) {
            int points = ctx.getPoints(owner);
            game.scoreFeature(points, ctx, owner);
        }
        for (Meeple m : ctx.getMeeples()) {
            m.undeploy(false);
        }

        super.checkDeployment(feature);
    }

    @Override
    public Feature getPieceForDeploy(Tile tile, Location loc) {
        Farm farmPiece = (Farm) tile.getFeaturePartOf(loc);
        if (farmPiece == null) {
            throw new IllegalArgumentException("No such farm");
        }
        return farmPiece;
    }


}
