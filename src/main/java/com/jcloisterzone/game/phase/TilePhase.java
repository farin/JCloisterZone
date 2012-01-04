package com.jcloisterzone.game.phase;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;

public class TilePhase extends Phase {


	public TilePhase(Game game) {
		super(game);
	}

	@Override
	public void enter() {
		Map<Position, Set<Rotation>> freezed = Maps.newHashMap(getBoard().getAvailablePlacements());
		game.getUserInterface().selectTilePlacement(freezed);
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
	public void placeTile(Rotation rotation, Position p) {
		Tile tile = getTile();
		tile.setRotation(rotation);
		
		boolean bridgeRequired = false;
		if (game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS)) {
			bridgeRequired = ! getBoard().isPlacementAllowed(tile, p);
		}

		getBoard().add(tile, p);
		if (tile.getTower() != null) {
			game.getTowerGame().registerTower(p);
		}		
		game.fireGameEvent().tilePlaced(tile);
		
		if (bridgeRequired) {
			BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
			Sites sites = bcb.prepareMandatoryBridgeAction().getSites();
			
			assert sites.size() == 1;
			Position pos = sites.keySet().iterator().next();
			Location loc = sites.get(pos).iterator().next();
			
			bcb.decreaseBridges(getActivePlayer());		
			bcb.deployBridge(pos, loc);
		}
		getBoard().mergeFeatures(tile);
		
		//TODO seperate tileMerged event here ??? and move this code to abbey and mayor game
		if (game.hasExpansion(Expansion.ABBEY_AND_MAYOR)) {
			Map<City, CityScoreContext> cityCache = Maps.newHashMap();
			for(Feature feature : getTile().getFeatures()) {
				if (feature instanceof Farm) {
					scoreFollowersOnBarnFarm((Farm) feature, cityCache);
				}
			}
		}
		
		next();
	}


}
