package com.jcloisterzone.game.phase;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;

public class ScorePhase extends Phase {

    private Set<Completable> alredyScored = Sets.newHashSet();

    public ScorePhase(Game game) {
        super(game);
    }

    private void scoreCompletedOnTile(Tile tile) {
        for(Feature feature : tile.getFeatures()) {
            if (feature instanceof Completable) {
                scoreCompleted((Completable) feature);
            }
        }
    }

    private void scoreCompletedNearAbbey(Position pos) {
        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(pos).entrySet()) {
            Tile tile = e.getValue();
            Feature feature = tile.getFeaturePartOf(e.getKey().rev());
            if (feature instanceof Completable) {
                scoreCompleted((Completable) feature);
            }
        }
    }

    private void scoreFollowersOnBarnFarm(Farm farm, Map<City, CityScoreContext> cityCache) {
        FarmScoreContext ctx = farm.getScoreContext();
        ctx.setCityCache(cityCache);
        farm.walk(ctx);

        boolean hasBarn = false;
        for(Meeple m : ctx.getSpecialMeeples()) {
            if (m instanceof Barn) {
                hasBarn = true;
                break;
            }
        }
        if (hasBarn) {
            for(Player p : ctx.getMajorOwners()) {
                int points = ctx.getPointsWhenBarnIsConnected(p);
                game.scoreFeature(points, ctx, p);
            }
            for(Meeple m : ctx.getMeeples()) {
                if (! (m instanceof Barn)) {
                    m.undeploy(false);
                }
            }
        }
    }

    @Override
    public void enter() {
        Position pos = getTile().getPosition();

        //TODO separate event here ??? and move this code to abbey and mayor game
        if (game.hasExpansion(Expansion.ABBEY_AND_MAYOR)) {
            Map<City, CityScoreContext> cityCache = Maps.newHashMap();
            for(Feature feature : getTile().getFeatures()) {
                if (feature instanceof Farm) {
                    scoreFollowersOnBarnFarm((Farm) feature, cityCache);
                }
            }
        }

        scoreCompletedOnTile(getTile());
        if (getTile().isAbbeyTile()) {
            scoreCompletedNearAbbey(pos);
        }

        if (game.hasExpansion(Expansion.TUNNEL)) {
            Road r = game.getTunnelGame().getPlacedTunnel();
            if (r != null) {
                scoreCompleted(r);
            }
        }

        for (Tile neighbour : getBoard().getAdjacentAndDiagonalTiles(pos)) {
            Cloister cloister = neighbour.getCloister();
            if (cloister != null) {
                scoreCompleted(cloister);
            }
        }

        if (game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS)) {
            BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
            for(Entry<Castle, Integer> entry : bcb.getCastleScore().entrySet()) {
                scoreCastle(entry.getKey(), entry.getValue());
            }
        }

        alredyScored.clear();

        next();
    }

    protected void undeployMeeples(CompletableScoreContext ctx) {
        for (Meeple m : ctx.getMeeples()) {
            m.undeploy(false);
        }
    }

    private void scoreCastle(Castle castle, int points) {
        List<Meeple> meeples = castle.getMeeples();
        if (meeples.isEmpty()) meeples = castle.getSecondFeature().getMeeples();
        Meeple m = meeples.get(0); //all meeples must share same owner
        m.getPlayer().addPoints(points, PointCategory.CASTLE);
        game.fireGameEvent().scored(m.getFeature(), points, points+"", m, false);
        m.undeploy(false);
    }

    private void scoreCompleted(Completable completable) {
        CompletableScoreContext ctx = completable.getScoreContext();
        completable.walk(ctx);
        if (game.hasExpansion(Expansion.TRADERS_AND_BUILDERS)) {
            for(Meeple m : ctx.getSpecialMeeples()) {
                if (m instanceof Builder && m.getPlayer().equals(getActivePlayer())) {
                    if (! m.getPosition().equals(getTile().getPosition())) {
                        game.getTradersAndBuildersGame().builderUsed();
                    }
                    break;
                }
            }
        }
        if (ctx.isCompleted()) {
            Completable master = (Completable) ctx.getMasterFeature();
            if (!alredyScored.contains(master)) {
                alredyScored.add(master);
                game.expansionDelegate().scoreCompleted(ctx);
                game.scoreCompletableFeature(ctx);
                undeployMeeples(ctx);
                game.fireGameEvent().completed(master, ctx);
            }
        }
    }

}
