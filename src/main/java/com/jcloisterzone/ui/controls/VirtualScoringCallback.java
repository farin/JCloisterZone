package com.jcloisterzone.ui.controls;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.score.ScoreAllCallback;
import com.jcloisterzone.feature.score.ScoringStrategy;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.feature.visitor.score.ScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class VirtualScoringCallback implements ScoreAllCallback {

	private Game game;	
	private ScoringStrategy scoringStrategy;
	private Map<Player, PlayerPanel> map = new HashMap<Player, PlayerPanel>();
	
    public VirtualScoringCallback(Game game, PlayerPanel[] playerPanels) {
        this.game = game;
        
        for (PlayerPanel playerPanel : playerPanels) {
        	map.put(playerPanel.getPlayer(), playerPanel);
        	
        	playerPanel.setVirtualPoints(playerPanel.getPlayer().getPoints());
        }
        
        scoringStrategy = new ScoringStrategy() {			
			@Override
			public void addPoints(Player player, int points, PointCategory tradeGoods) {
				PlayerPanel playerPanel = map.get(player);
				
				if (playerPanel != null) {
					playerPanel.addVirtualPoints(points);
				}
			}
		};
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
        map.get(meeple.getPlayer()).addVirtualPoints(points);
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
    
    @Override
	public void scoreCapabilities() {
		for (Capability capability : game.getCapabilities()) {
			capability.finalScoring(scoringStrategy);
		}
	}

	private void virtualScoreFeature(int points, ScoreContext ctx, Player p) {
		PlayerPanel playerPanel = map.get(p);
		
		if (playerPanel != null) {
			playerPanel.addVirtualPoints(points);
		}
    }
    
    @Override
    public CompletableScoreContext getCompletableScoreContext(Completable completable) {
        return completable.getScoreContext();
    }

    @Override
    public FarmScoreContext getFarmScoreContext(Farm farm) {
        return farm.getScoreContext();
    }
}