package com.jcloisterzone.game.phase;

import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.SiegeCapability;


public class EscapePhase extends Phase {

    public EscapePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(SiegeCapability.class);
    }

    @Override
    public void enter() {
        UndeployAction action = prepareEscapeAction();
        if (prepareEscapeAction() != null) {
            notifyUI(action, true);
        } else {
            next();
        }
    }

    @Override
    public void pass() {
        next();
    }

    private class FindNearbyCloister implements FeatureVisitor<Boolean> {

        private boolean result;


        public Boolean getResult() {
            return result;
        }

        @Override
        public boolean visit(Feature feature) {
            City city = (City) feature;
            if (city.isBesieged()) { //cloister must border Cathar tile
                Position p = city.getTile().getPosition();
                for (Tile tile : getBoard().getAdjacentAndDiagonalTiles(p)) {
                    if (tile.hasCloister()) {
                        result = true;
                        return false; //do not continue, besieged cloister exists
                    }
                }
            }
            return true;
        }
    }


    public UndeployAction prepareEscapeAction() {
        UndeployAction escapeAction = null;
        for (Meeple m : game.getDeployedMeeples()) {
            if (m.getPlayer() != getActivePlayer()) continue;
            if (!(m.getFeature() instanceof City)) continue;
            if (m.getFeature().walk(new FindNearbyCloister())) {
                if (escapeAction == null) {
                    escapeAction = new UndeployAction("escape", PlayerRestriction.only(getActivePlayer()));
                }
                escapeAction.getOrCreate(m.getPosition()).add(m.getLocation());
            }
        }
        return escapeAction;
    }


    @Override
    public void undeployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        assert meepleOwner == getActivePlayer().getIndex();
        Meeple m = game.getMeeple(p, loc, meepleType, game.getPlayer(meepleOwner));
        if (!(m.getFeature() instanceof City)) {
            logger.error("Feature for escape action must be a city");
            return;
        }
        m.undeploy();
        next();
    }

}
