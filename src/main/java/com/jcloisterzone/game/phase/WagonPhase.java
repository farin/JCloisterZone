package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.feature.visitor.IsOccupiedOrCompleted;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.ui.GameController;


public class WagonPhase extends ServerAwarePhase {

    final WagonCapability wagonCap;


    public WagonPhase(Game game, GameController controller) {
        super(game, controller);
        wagonCap = game.getCapability(WagonCapability.class);
    }


    @Override
    public boolean isActive() {
        return game.hasCapability(WagonCapability.class);
    }

    @Override
    public void enter() {
        if (!existsLegalMove()) next();
    }

    @Override
    public void pass() {
        Player player = wagonCap.getWagonPlayer();
        wagonCap.removeScoredWagon(player);
        enter();
    }

    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        if (!meepleType.equals(Wagon.class)) {
            logger.error("Illegal figure type.");
            return;
        }
        Player player = wagonCap.getWagonPlayer();
        Meeple m = player.getMeepleFromSupply(Wagon.class);
        m.deployUnoccupied(getBoard().get(p), loc);
        wagonCap.removeScoredWagon(player);
        enter();
    }

    @Override
    public Player getActivePlayer() {
        Player p = wagonCap.getWagonPlayer();
        return p == null ? game.getTurnPlayer() : p;
    }

    private boolean existsLegalMove() {
        Map<Player, Feature> rw = wagonCap.getScoredWagons();
        Player wagonPlayer;
        while ((wagonPlayer = wagonCap.getWagonPlayer()) != null) {
            Feature f = rw.get(wagonPlayer);
            List<FeaturePointer> wagonMoves = prepareWagonMoves(f);
            if (!wagonMoves.isEmpty()) {
                Player activePlayer = getActivePlayer();
                toggleClock(activePlayer);
                game.post(new SelectActionEvent(activePlayer, new MeepleAction(Wagon.class).addAll(wagonMoves), true));
                return true;
            } else {
                rw.remove(wagonPlayer);
            }
        }
        return false;
    }

    private List<FeaturePointer> prepareWagonMoves(Feature source) {
        if (source.getTile().isAbbeyTile()) {
            List<FeaturePointer> wagonMoves = new ArrayList<>();
            for (Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(source.getTile().getPosition()).entrySet()) {
                Tile tile = entry.getValue();
                Feature f = tile.getFeaturePartOf(entry.getKey().rev());
                if (f == null || f.walk(new IsOccupiedOrCompleted())) continue;
                wagonMoves.add(new FeaturePointer(tile.getPosition(), f.getLocation()));
            }
            return wagonMoves;
        } else {
            return source.walk(new FindUnoccupiedNeighbours());
        }
    }

    private class FindUnoccupiedNeighbours implements FeatureVisitor<List<FeaturePointer>> {

        private List<FeaturePointer> wagonMoves = new ArrayList<>();

        @Override
        public VisitResult visit(Feature feature) {
            if (feature instanceof MultiTileFeature) {
                MultiTileFeature f = (MultiTileFeature) feature;
                MultiTileFeature[] edges = f.getEdges();
                for (int i = 0; i < edges.length; i++) {
                    if (edges[i] == f) { //special value - neigbouring abbey
                        int j = 0;
                        for (Location side : Location.sides()) {
                            if (side.intersect(f.getLocation()) != null) {
                                if (j == i) {
                                    //Abbey at side;
                                    Position target = f.getTile().getPosition().add(side);
                                    Tile abbeyTile = getBoard().get(target);
                                    assert abbeyTile.isAbbeyTile();
                                    if (!abbeyTile.getCloister().walk(new IsOccupiedOrCompleted())) {
                                        wagonMoves.add(new FeaturePointer(target, Location.CLOISTER));
                                    }
                                }
                                j++;
                            }
                        }
                        break;
                    }
                }
            }

            if (feature.getNeighbouring() != null) {
                for (Feature nei : feature.getNeighbouring()) {
                    if (nei instanceof Cloister) {
                        Cloister cloister = (Cloister) nei;
                        if (cloister.isMonastery() && cloister.getMeeples().isEmpty()) {
                            wagonMoves.add(new FeaturePointer(nei.getTile().getPosition(), Location.ABBOT));
                        }
                    }
                    if (nei.walk(new IsOccupiedOrCompleted())) continue;
                    wagonMoves.add(new FeaturePointer(nei.getTile().getPosition(), nei.getLocation()));
                }
            }
            return VisitResult.CONTINUE;
        }

        @Override
		public List<FeaturePointer> getResult() {
            return wagonMoves;
        }
    }

}
