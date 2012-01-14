package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.CastleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
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
		CastleAction action = new CastleAction(getTile().getPosition(), locs);
		game.getUserInterface().selectAction(Collections.<PlayerAction>singletonList(action));		
	}
	
	@Override
	public void placeNoFigure() {
		prepareCastleAction();
	}
	
	@Override
	public void deployCastle(Position pos, Location loc) {		
		BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
		Player owner = bcb.getCastlePlayer();
		bcb.decreaseCastles(owner);
		Castle castle1 = replaceCityWithCastle(getBoard().get(pos), loc);
		Castle castle2 = replaceCityWithCastle(getBoard().get(pos.add(loc)), loc.rev());
		castle1.getEdges()[0] = castle2;
		castle2.getEdges()[0] = castle1;
		game.fireGameEvent().castleDeployed(castle1, castle2);
					
		prepareCastleAction(); //it is possible to deploy castle by another player  
	}
		
	private Castle replaceCityWithCastle(Tile tile, Location loc) {
		ListIterator<Feature> iter = tile.getFeatures().listIterator();
		City city = null;
		while(iter.hasNext()) {
			Feature feature =  iter.next();
			if (feature.getLocation() == loc) {
				city = (City) feature;
				break;
			}
		}
		Meeple m = city.getMeeple();
		if (m != null) {
			m.undeploy(false);
		}
		Castle castle = new Castle();
		castle.setTile(tile);
		castle.setId(game.idSequnceNextVal());
		castle.setLocation(loc.rotateCCW(tile.getRotation()));
		iter.set(castle);
		
		for(Feature f : tile.getFeatures()) { //replace also city references
			if (f instanceof Farm) {
				Farm farm = (Farm) f;
				Feature[] adjoining = farm.getAdjoiningCities();
				if (adjoining != null) {
					for(int i = 0; i < adjoining.length; i++) {
						if (adjoining[i] == city) {
							adjoining[i] = castle;
							break;
						}
					}
				}
			}
		}
		
		if (m != null) {
			m.deploy(tile, loc);
		}
		return castle;
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
