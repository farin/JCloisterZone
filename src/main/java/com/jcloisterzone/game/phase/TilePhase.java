package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;

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
		Tile nextTile = getTile();
		nextTile.setRotation(rotation);
		
		boolean bridgeRequired = false;
		if (game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS)) {
			bridgeRequired = ! getBoard().isPlacementAllowed(nextTile, p);
		}

		getBoard().add(nextTile, p);
		if (nextTile.getTower() != null) {
			game.getTowerGame().registerTower(p);
		}		
		game.fireGameEvent().tilePlaced(nextTile);
		
		if (bridgeRequired) {
			BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
			BridgeAction action = bcb.prepareMandatoryBridgeAction();
			game.getUserInterface().selectAction(Collections.<PlayerAction>singletonList(action));
		} else {
			postPlacement();
		}
	}
	
	@Override
	public void deployBridge(Position pos, Location loc) {
		BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
		bcb.decreaseBridges(getActivePlayer());		
		bcb.deployBridge(pos, loc);
		postPlacement();		
	}
	
	private void postPlacement() {
		Tile tile = getTile();
		
		getBoard().mergeFeatures(tile);

		if (game.hasExpansion(Expansion.ABBEY_AND_MAYOR)) {
			Map<City, CityScoreContext> cityCache = Maps.newHashMap();
			for(Feature feature : getTile().getFeatures()) {
				if (feature instanceof Farm) {
					scoreFollowersOnBarnFarm((Farm) feature, cityCache);
				}
			}
		}
			
		if (tile.getTrigger() == TileTrigger.VOLCANO) {
			PrincessAndDragonGame pd = game.getPrincessAndDragonGame();
			pd.setDragonPosition(getTile().getPosition());
			game.getTilePack().activateGroup("dragon");
			game.fireGameEvent().dragonMoved(getTile().getPosition());
		}
		next();
	}

}
