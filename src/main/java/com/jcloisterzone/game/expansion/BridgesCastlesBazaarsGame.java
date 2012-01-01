package com.jcloisterzone.game.expansion;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.game.ExpandedGame;

public class BridgesCastlesBazaarsGame extends ExpandedGame {
	
	private boolean bridgeUsed;
	private Map<Player, Integer> bridges = Maps.newHashMap();
	private Map<Player, Integer> castles = Maps.newHashMap();
	
	@Override
	public void initPlayer(Player player) {		
		int players = game.getAllPlayers().length;
		if (players < 5) {
			bridges.put(player, 3);
			castles.put(player, 3);
		} else {
			bridges.put(player, 2);
			castles.put(player, 2);
		}
	}
	
	@Override
	public void initTile(Tile tile, Element xml) {
		if (xml.getElementsByTagName("bazaar").getLength() > 0) {
			tile.setTrigger(TileTrigger.BAZAAR);
		}		
	}
	
	@Override
	public void turnCleanUp() {
		bridgeUsed = false;
	}
	
	@Override
	public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
		//TODO allow only one bridge per turn
		if (! bridgeUsed && getPlayerBridges(game.getPhase().getActivePlayer()) > 0) {
			BridgeAction action = prepareBridgeAction();
			if (action != null) {
				actions.add(action);
			}
		} 
	}
	
	public BridgeAction prepareMandatoryBridgeAction() {
		return prepareBridgeAction(); 
	}
	
	private BridgeAction prepareBridgeAction() {
		BridgeAction action = null;		
		Tile tile = game.getTile();		
		action = prepareTileBridgeAction(tile, action);
		for(Tile adjacent : getBoard().getAdjacentTilesMap(tile.getPosition()).values()) {
			action = prepareTileBridgeAction(adjacent, action);
		}
		return action;
	}
	
	private BridgeAction prepareTileBridgeAction(Tile tile, BridgeAction action) {
		action = prepareTileBridgeAction(tile, action, Location.NS);
		action = prepareTileBridgeAction(tile, action, Location.WE);
		return action;
	}
		
	private BridgeAction prepareTileBridgeAction(Tile tile, BridgeAction action, Location bridgeLoc) {
		if (! tile.isBridgeAllowed(bridgeLoc)) return action;		
		for(Location side : bridgeLoc.intersectMulti(Location.sides())) {
			Tile adjacent = getBoard().get(tile.getPosition().add(side));
			if (adjacent != null) {
				if (adjacent.getEdgePattern().at(side.rev(), adjacent.getRotation()) != 'R') {
					return action;
				}
			}
		}					
		if (action == null) action = new BridgeAction();
		action.getSites().getOrCreate(tile.getPosition()).add(bridgeLoc);			
		return action;
	}
	
	@Override
	public boolean isSpecialPlacementAllowed(Tile tile, Position p) {
		if (getPlayerBridges(game.getActivePlayer()) > 0) {			
			if (isBridgePlacementAllowed(tile, p, Location.NS)) return true;
			if (isBridgePlacementAllowed(tile, p, Location.WE)) return true;
		}
		return false;
	}
		
	private boolean isBridgePlacementAllowed(Tile tile, Position p, Location bridge) {
		if (! tile.isBridgeAllowed(bridge)) return false;
		EdgePattern pattern = tile.getEdgePattern().getBridgePattern(bridge.rotateCCW(tile.getRotation()));
		for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {
			Tile adjacent = e.getValue();
			Location rel = e.getKey();
			char tileSide = pattern.at(rel, tile.getRotation());
			char adjacentSide = adjacent.getEdgePattern().at(rel.rev(), adjacent.getRotation()); 
			if (tileSide != adjacentSide) {
				return false;
			}
		}
		return true;
	}
	
	public int getPlayerCastles(Player pl) {
		return castles.get(pl);
	}
	
	public int getPlayerBridges(Player pl) {
		return bridges.get(pl);
	}
	
	public void decreaseCastles(Player player) {
		int n = getPlayerCastles(player);
		if (n == 0) throw new IllegalStateException("Player has no castles");
		castles.put(player, n-1);
	}
	
	public void decreaseBridges(Player player) {
		int n = getPlayerBridges(player);
		if (n == 0) throw new IllegalStateException("Player has no bridges");
		bridges.put(player, n-1);
	}
	
	public void deployBridge(Position pos, Location loc) {
		Tile tile = getBoard().get(pos);
		if (! tile.isBridgeAllowed(loc)) {
			throw new IllegalArgumentException("Cannot deploy " + loc + " bridge on " + pos);
		}
		bridgeUsed = true;
		tile.placeBridge(loc);
		game.fireGameEvent().bridgeDeployed(pos, loc);
	}
	
	@Override
	public BridgesCastlesBazaarsGame copy() {
		BridgesCastlesBazaarsGame copy = new BridgesCastlesBazaarsGame();
		copy.game = game;
		copy.bridges = Maps.newHashMap(bridges);
		copy.castles = Maps.newHashMap(castles);
		return copy;
	}	
	
	@Override
	public void saveToSnapshot(Document doc, Element node) {	
		node.setAttribute("bridgeUsed", bridgeUsed + "");
		for(Player player: game.getAllPlayers()) {
			Element el = doc.createElement("player");
			node.appendChild(el);
			el.setAttribute("index", "" + player.getIndex());
			el.setAttribute("castles", "" + getPlayerCastles(player));
			el.setAttribute("bridges", "" + getPlayerBridges(player));			
		}
	}
	
	@Override
	public void loadFromSnapshot(Document doc, Element node) {		
		bridgeUsed = Boolean.parseBoolean(node.getAttribute("bridgeUsed"));
		NodeList nl = node.getElementsByTagName("player");
		for(int i = 0; i < nl.getLength(); i++) {
			Element playerEl = (Element) nl.item(i);
			Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
			castles.put(player, Integer.parseInt(playerEl.getAttribute("castles")));			
			bridges.put(player, Integer.parseInt(playerEl.getAttribute("bridges")));
		}
	}
}