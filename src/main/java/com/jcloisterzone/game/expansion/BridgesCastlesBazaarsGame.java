package com.jcloisterzone.game.expansion;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.ExpandedGame;

public class BridgesCastlesBazaarsGame extends ExpandedGame {
	
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
	
	public int getPlayerCastles(Player pl) {
		return castles.get(pl);
	}
	
	public int getPlayerBridges(Player pl) {
		return bridges.get(pl);
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
		NodeList nl = node.getElementsByTagName("player");
		for(int i = 0; i < nl.getLength(); i++) {
			Element playerEl = (Element) nl.item(i);
			Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
			castles.put(player, Integer.parseInt(playerEl.getAttribute("castles")));			
			bridges.put(player, Integer.parseInt(playerEl.getAttribute("bridges")));
		}
	}
}