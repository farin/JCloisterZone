package com.jcloisterzone.game.phase;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.CastleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;

public class CastlePhase extends Phase {

    public CastlePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS);
    }

    @Override
    public Player getActivePlayer() {
        Player p = game.getBridgesCastlesBazaarsGame().getCastlePlayer();
        return p == null ? game.getTurnPlayer() : p;
    }

    @Override
    public void enter() {
        BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
        Tile tile = getTile();
        Map<Player, Set<Location>> currentTileCastleBases = null;
        for(Feature f : tile.getFeatures()) {
            if (!(f instanceof City)) continue;
            Player owner = f.walk(new FindCastleBaseVisitor());
            if (owner == null || bcb.getPlayerCastles(owner) == 0) continue;
            if (currentTileCastleBases == null) currentTileCastleBases = Maps.newHashMap();
            Set<Location> locs = currentTileCastleBases.get(owner);
            if (locs == null) {
                locs = Sets.newHashSet();
                currentTileCastleBases.put(owner, locs);
            }
            locs.add(f.getLocation());
        }
        if (currentTileCastleBases == null) {
            next();
            return;
        }
        bcb.setCurrentTileCastleBases(currentTileCastleBases);
        prepareCastleAction();
    }

    private void prepareCastleAction() {
        BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
        Map<Player, Set<Location>> currentTileCastleBases = bcb.getCurrentTileCastleBases();
        if (currentTileCastleBases.isEmpty()) {
            bcb.setCastlePlayer(null);
            bcb.setCurrentTileCastleBases(null);
            next();
            return;
        }
        int pi = game.getTurnPlayer().getIndex();
        while(! currentTileCastleBases.containsKey(game.getAllPlayers()[pi])) {
            pi++;
            if (pi == game.getAllPlayers().length) pi = 0;
        }
        Player player = game.getAllPlayers()[pi];
        bcb.setCastlePlayer(player);
        Set<Location> locs = currentTileCastleBases.remove(player);
        notifyUI(new CastleAction(getTile().getPosition(), locs), true);
    }

    @Override
    public void pass() {
        prepareCastleAction();
    }

    @Override
    public void deployCastle(Position pos, Location loc) {
        BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
        Player owner = bcb.getCastlePlayer();
        bcb.decreaseCastles(owner);
        bcb.convertCityToCastle(pos, loc);
        prepareCastleAction(); //it is possible to deploy castle by another player
    }

    class FindCastleBaseVisitor implements FeatureVisitor<Player> {

        int size = 0;
        boolean castleBase = true;
        Player owner;

        @Override
        public boolean visit(Feature feature) {
            City c = (City) feature;
            if (! c.isCastleBase()) {
                castleBase = false;
                return false;
            }
            if (c.getMeeple() instanceof Follower) {
                owner = c.getMeeple().getPlayer();
            }
            size++;
            if (size > 2) return false;
            return true;
        }

        public Player getResult() {
            if (castleBase && size == 2) return owner;
            return null;
        }

    }

}
