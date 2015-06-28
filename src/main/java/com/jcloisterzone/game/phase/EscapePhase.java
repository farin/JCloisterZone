package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;
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
            game.post(new SelectActionEvent(getActivePlayer(), action, true));
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

        @Override
        public Boolean getResult() {
            return result;
        }

        @Override
        public VisitResult visit(Feature feature) {
            City city = (City) feature;
            if (city.isBesieged()) { //cloister must border Cathar tile
                Position p = city.getTile().getPosition();
                for (Tile tile : getBoard().getAdjacentAndDiagonalTiles(p)) {
                    if (tile.hasCloister()) {
                        result = true;
                        return VisitResult.STOP; //do not continue, besieged cloister exists
                    }
                }
            }
            return VisitResult.CONTINUE;
        }
    }

    private class FindNearbyCloisterRgg implements FeatureVisitor<Boolean> {
        private boolean isBesieged;
        private boolean cloisterExists;

        @Override
        public Boolean getResult() {
            return isBesieged && cloisterExists;
        }

        @Override
        public VisitResult visit(Feature feature) {
            City city = (City) feature;
            if (city.isBesieged()) {
                isBesieged = true;
            }

            Position p = city.getTile().getPosition();
            for (Tile tile : getBoard().getAdjacentAndDiagonalTiles(p)) {
                if (tile.hasCloister()) {
                    cloisterExists = true;
                    break;
                }
            }
            return VisitResult.CONTINUE;
        }
    }


    public UndeployAction prepareEscapeAction() {
        UndeployAction escapeAction = null;
        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m instanceof Follower)) continue;
            if (m.getPlayer() != getActivePlayer()) continue;
            if (!(m.getFeature() instanceof City)) continue;

            FeatureVisitor<Boolean> visitor = game.getBooleanValue(CustomRule.ESCAPE_RGG) ? new FindNearbyCloisterRgg() : new FindNearbyCloister();
            if (m.getFeature().walk(visitor)) {
                if (escapeAction == null) {
                    escapeAction = new UndeployAction(SiegeCapability.UNDEPLOY_ESCAPE);
                }
                escapeAction.add(new MeeplePointer(m));
            }
        }
        return escapeAction;
    }


    @Override
    public void undeployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        assert meepleOwner == getActivePlayer().getIndex();
        Meeple m = game.getMeeple(fp, meepleType, game.getPlayer(meepleOwner));
        if (!(m.getFeature() instanceof City)) {
            logger.error("Feature for escape action must be a city");
            return;
        }
        m.undeploy();
        next();
    }

}
