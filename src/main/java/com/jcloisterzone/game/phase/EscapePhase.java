package com.jcloisterzone.game.phase;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.action.EscapeAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;


public class EscapePhase extends Phase {

    public EscapePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasExpansion(Expansion.CATHARS);
    }

    @Override
    public void enter() {
        EscapeAction action = prepareEscapeAction();
        if (prepareEscapeAction() != null) {
            notifyUI(action, true);
        } else {
            next();
        }
    }

    private class FindNearbyCloister implements FeatureVisitor<Boolean> {

        private boolean isBesieged, cloisterNearby;


        public Boolean getResult() {
            return isBesieged && cloisterNearby;
        }

        @Override
        public boolean visit(Feature feature) {
            City city = (City) feature;
            if (city.isBesieged()) { //cloister must border Cathar tile
                isBesieged = true;
                Position p = city.getTile().getPosition();
                for(Tile tile : getBoard().getAdjacentAndDiagonalTiles(p)) {
                    if (tile.hasCloister()) {
                        cloisterNearby = true;
                        return false; //do not continue, besieged cloister exists
                    }
                }
            }
            return true;
        }
    }


    public EscapeAction prepareEscapeAction() {
        EscapeAction escapeAction = null;
        for(Meeple m : game.getDeployedMeeples()) {
            if (m.getPlayer() != getActivePlayer()) continue;
            if (! (m.getFeature() instanceof City)) continue;
            if (m.getFeature().walk(new FindNearbyCloister())) {
                if (escapeAction == null) {
                    escapeAction = new EscapeAction();
                }
                escapeAction.getOrCreate(m.getPosition()).add(m.getLocation());
            }
        }
        return escapeAction;
    }


    @Override
    public void undeployMeeple(Position p, Location loc) {
        Meeple m = game.getMeeple(p, loc);
        if (! (m.getFeature() instanceof City)) {
            logger.error("Feature for escape action must be a city");
            return;
        }
        m.undeploy();
        next();
    }

}
