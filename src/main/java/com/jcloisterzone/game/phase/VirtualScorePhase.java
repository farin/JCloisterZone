package com.jcloisterzone.game.phase;

import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.score.ScoreAllCallback;
import com.jcloisterzone.feature.score.ScoreAllFeatureFinder;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.feature.visitor.score.ScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public class VirtualScorePhase extends Phase implements ScoreAllCallback {

    public VirtualScorePhase(Game game) {
        super(game);
    }

    @Override
    public void enter() {
//        FairyCapability fairyCap = game.getCapability(FairyCapability.class);
//        if (fairyCap != null) {
//            //erase position to not affect final scoring
//            fairyCap.setFairyPosition(null);
//        }
    	
    	for (Player p : game.getAllPlayers()) {
    		p.setVirtualPoints(p.getPoints());
    	}

        ScoreAllFeatureFinder scoreAll = new ScoreAllFeatureFinder();
        scoreAll.scoreAll(game, this);

        game.virtualScoring();
        
        next();
    }

    @Override
    public void scoreCastle(Meeple meeple, Castle castle) {
//        ScoreEvent ev = new ScoreEvent(meeple.getFeature(), 0, PointCategory.CASTLE, meeple);
//        ev.setFinal(true);
//        game.post(ev);
    }

    @Override
    public void scoreFarm(FarmScoreContext ctx, Player p) {
        virtualScoreFeature(ctx.getPoints(p), ctx, p);
    }

    @Override
    public void scoreBarn(FarmScoreContext ctx, Barn meeple) {
        int points = ctx.getBarnPoints();
        meeple.getPlayer().addVirtualPoints(points, PointCategory.FARM);
    }


    @Override
    public void scoreCompletableFeature(CompletableScoreContext ctx) {
    	Set<Player> players = ctx.getMajorOwners();
        if (players.isEmpty()) return;
        
        int points = ctx.getPoints();
        for (Player p : players) {
        	virtualScoreFeature(points, ctx, p);
        }
    }

    private void virtualScoreFeature(int points, ScoreContext ctx, Player p) {
    	p.addVirtualPoints(points, ctx.getMasterFeature().getPointCategory());
    }
    
    @Override
    public CompletableScoreContext getCompletableScoreContext(Completable completable) {
        return completable.getScoreContext();
    }

    @Override
    public FarmScoreContext getFarmScoreContext(Farm farm) {
        return farm.getScoreContext();
    }

    @Override
    public Player getActivePlayer() {
        return null;
    }

}