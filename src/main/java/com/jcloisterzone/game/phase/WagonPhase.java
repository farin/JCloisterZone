package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.jcloisterzone.feature.visitor.IsOccupied;
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
    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
        if (!meepleType.equals(Wagon.class)) {
            logger.error("Illegal figure type.");
            return;
        }
        Player player = wagonCap.getWagonPlayer();
        Meeple m = player.getMeepleFromSupply(Wagon.class);
        if (getBoard().get(fp).walk(new IsOccupied())) {
            throw new IllegalArgumentException("Feature is occupied.");
        }
        m.deploy(fp);
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
            Set<FeaturePointer> wagonMoves = prepareWagonMoves(f);
            if (!wagonMoves.isEmpty()) {
                Player activePlayer = getActivePlayer();
                toggleClock(activePlayer);
                game.post(new SelectActionEvent(activePlayer, new MeepleAction(Wagon.class).addAll(wagonMoves), true, false));
                return true;
            } else {
                rw.remove(wagonPlayer);
            }
        }
        return false;
    }

    private List<FeaturePointer> getPlacements(Feature f) {
        if (f == null) return Collections.emptyList();
        CollectingIsOccupiedOrCompleted visitor = new CollectingIsOccupiedOrCompleted();
        f.walk(visitor);
        return visitor.getPlacements();
    }

    private Set<FeaturePointer> prepareWagonMoves(Feature source) {
        if (source.getTile().isAbbeyTile()) {
            Set<FeaturePointer> wagonMoves = new HashSet<>();
            for (Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(source.getTile().getPosition()).entrySet()) {
                Tile tile = entry.getValue();
                Feature f = tile.getFeaturePartOf(entry.getKey().rev());
                wagonMoves.addAll(getPlacements(f));
            }
            return wagonMoves;
        } else {
            return source.walk(new FindUnoccupiedNeighbours());
        }
    }

    private class CollectingIsOccupiedOrCompleted extends IsOccupiedOrCompleted {
        List<FeaturePointer> placements = new ArrayList<FeaturePointer>();

        @Override
        public VisitResult visit(Feature feature) {
            if (game.isDeployAllowed(feature.getTile(), Wagon.class)) {
                placements.add(new FeaturePointer(feature));
            }
            return super.visit(feature);
        }

        public List<FeaturePointer> getPlacements() {
            if (getResult()) {
                return Collections.emptyList();
            }
            return placements;
        }
    }

    private class FindUnoccupiedNeighbours implements FeatureVisitor<Set<FeaturePointer>> {

        private Set<FeaturePointer> wagonMoves = new HashSet<>();

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
                                    wagonMoves.addAll(getPlacements(abbeyTile.getCloister()));
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
                    Tile tile = nei.getTile();

                    if ((nei instanceof Cloister) && game.isDeployAllowed(tile, Wagon.class)) {
                        Cloister cloister = (Cloister) nei;
                        if (cloister.isMonastery() && cloister.getMeeples().isEmpty()) {
                            wagonMoves.add(new FeaturePointer(tile.getPosition(), Location.ABBOT));
                        }
                    }
                    wagonMoves.addAll(getPlacements(nei));
                }
            }
            return VisitResult.CONTINUE;
        }

        @Override
        public Set<FeaturePointer> getResult() {
            return wagonMoves;
        }
    }

}
