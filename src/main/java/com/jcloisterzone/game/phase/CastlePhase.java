package com.jcloisterzone.game.phase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.CastleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.CastleCapability;

public class CastlePhase extends Phase {

    private final CastleCapability castleCap;

    public CastlePhase(Game game) {
        super(game);
        castleCap = game.getCapability(CastleCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(CastleCapability.class);
    }

    @Override
    public Player getActivePlayer() {
        Player p = castleCap.getCastlePlayer();
        return p == null ? game.getTurnPlayer() : p;
    }

    @Override
    public void enter() {
        Tile tile = getTile();
        Map<Player, Set<Location>> currentTileCastleBases = null;
        for (Feature f : tile.getFeatures()) {
            if (!(f instanceof City)) continue;
            Player owner = f.walk(new FindCastleBaseVisitor());
            if (owner == null || castleCap.getPlayerCastles(owner) == 0) continue;
            if (currentTileCastleBases == null) currentTileCastleBases = new HashMap<>();
            Set<Location> locs = currentTileCastleBases.get(owner);
            if (locs == null) {
                locs = new HashSet<>();
                currentTileCastleBases.put(owner, locs);
            }
            locs.add(f.getLocation());
        }
        if (currentTileCastleBases == null) {
            next();
            return;
        }
        castleCap.setCurrentTileCastleBases(currentTileCastleBases);
        prepareCastleAction();
    }

    private void prepareCastleAction() {
        Map<Player, Set<Location>> currentTileCastleBases = castleCap.getCurrentTileCastleBases();
        if (currentTileCastleBases.isEmpty()) {
            castleCap.setCastlePlayer(null);
            castleCap.setCurrentTileCastleBases(null);
            next();
            return;
        }
        int pi = game.getTurnPlayer().getIndex();
        while(! currentTileCastleBases.containsKey(game.getAllPlayers()[pi])) {
            pi++;
            if (pi == game.getAllPlayers().length) pi = 0;
        }
        Player player = game.getAllPlayers()[pi];
        castleCap.setCastlePlayer(player);
        Set<Location> locs = currentTileCastleBases.remove(player);
        notifyUI(new CastleAction(getTile().getPosition(), locs), true);
    }

    @Override
    public void pass() {
        prepareCastleAction();
    }

    @Override
    public void deployCastle(Position pos, Location loc) {
        Player owner = castleCap.getCastlePlayer();
        castleCap.decreaseCastles(owner);
        castleCap.convertCityToCastle(pos, loc);
        prepareCastleAction(); //it is possible to deploy castle by another player
    }

    class FindCastleBaseVisitor implements FeatureVisitor<Player> {

        int size = 0;
        boolean castleBase = true;
        Player owner;

        @Override
        public boolean visit(Feature feature) {
            City c = (City) feature;
            if (!c.isCastleBase()) {
                castleBase = false;
                return false;
            }
            //if more then one follower is on caste, all has same owner
            //possible scenario - deploy on city - add by crop circle another follower - deploy castle
            if (!c.getMeeples().isEmpty()) {
                owner = c.getMeeples().get(0).getPlayer();
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
