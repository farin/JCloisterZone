package com.jcloisterzone.game.expansion;

import static com.jcloisterzone.board.XmlUtils.attributeBoolValue;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Sets;
import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.PrincessAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.XmlUtils;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.ExpandedGame;


public final class PrincessAndDragonGame extends ExpandedGame {

	public static final int DRAGON_MOVES = 6;
	public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

	public Position dragonPosition;
	public int dragonMovesLeft;
	public int dragonPlayer; //TODO player obj ?
	public Set<Position> dragonVisitedTiles;

	public Position fairyPosition;


	public Position getFairyPosition() {
		return fairyPosition;
	}

	public void setFairyPosition(Position fairyPosition) {
		this.fairyPosition = fairyPosition;
	}

	public Position getDragonPosition() {
		return dragonPosition;
	}

	public void setDragonPosition(Position dragonPosition) {
		this.dragonPosition = dragonPosition;
	}

	public int getDragonPlayer() {
		return dragonPlayer;
	}

	public int getDragonMovesLeft() {
		return dragonMovesLeft;
	}


	public Set<Position> getDragonVisitedTiles() {
		return dragonVisitedTiles;
	}

	public void triggerDragonMove() {
		dragonMovesLeft = DRAGON_MOVES;
		dragonPlayer = game.getTurnPlayer().getIndex();
		dragonVisitedTiles = Sets.newHashSet();
		dragonVisitedTiles.add(dragonPosition);
	}

	public void nextDragonPlayer() {
		dragonMovesLeft--;
		//TODO metoda na Game pro cycle
		dragonPlayer =
			dragonPlayer == (getGame().getAllPlayers().length - 1) ? 0 : dragonPlayer + 1;
	}

	public void endDragonMove() {
		dragonMovesLeft = 0;
		dragonVisitedTiles = null;
		dragonPlayer = -1;
	}

	public Set<Position> getAvailDragonMoves() {
		Set<Position> result = Sets.newHashSet();
		for(Position offset: Position.ADJACENT.values()) {
			Position position = dragonPosition.add(offset);
			Tile tile = getBoard().get(position);
			if (tile == null) continue;
			if (dragonVisitedTiles != null && dragonVisitedTiles.contains(position)) { continue; }
			if (position.equals(fairyPosition)) { continue; }
			result.add(position);
		}
		return result;
	}

	@Override
	public void initTile(Tile tile, Element xml) {
		if (xml.getElementsByTagName("volcano").getLength() > 0) {
			tile.setTrigger(TileTrigger.VOLCANO);
		}
		if (xml.getElementsByTagName("dragon").getLength() > 0) {
			tile.setTrigger(TileTrigger.DRAGON);
		}
		if (xml.getElementsByTagName("portal").getLength() > 0) {
			tile.setTrigger(TileTrigger.PORTAL);
		}
	}

	@Override
	public void initFeature(Tile tile, Feature feature, Element xml) {
		if (feature instanceof City && attributeBoolValue(xml, "princess")) {
			((City)feature).setPricenss(true);
		}
	}

	@Override
	public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
		prepareFairy(actions);
		preparePrincess(actions);
		if (TileTrigger.PORTAL.equals(getTile().getTrigger())) {
			if (game.getActivePlayer().hasFollower()) {
				prepareMagicPortal(commonSites);
			}
		}
	}

	private void prepareFairy(List<PlayerAction> actions) {
		FairyAction fairyAction = null;
		for(Follower m : game.getActivePlayer().getFollowers()) {
			if (m.getPosition() != null && ! m.getPosition().equals(fairyPosition)) {
				if (fairyAction == null) {
					fairyAction = new FairyAction();
					actions.add(fairyAction);
				}
				fairyAction.getSites().add(m.getPosition());
			}
		}
	}

	private void prepareMagicPortal(Sites commonSites) {
		for(Tile tile : getBoard().getAllTiles()) {
			if (tile == getTile()) continue; //prepared by basic common
			if (tile.getPosition().equals(dragonPosition)) continue;
			Set<Location> tileSites = getGame().prepareCommonForTile(tile, true);
			if (tileSites.isEmpty()) continue;
			commonSites.put(tile.getPosition(), tileSites);
		}
	}

	private void preparePrincess(List<PlayerAction> actions) {
		City c = getTile().getPrincessCityPiece();
		if (c == null || ! c.isFeatureOccupied()) return;
		Feature cityRepresentative = c.getRepresentativeFeature();

		PrincessAction princessAction = new PrincessAction();
		for(Meeple m : getGame().getDeployedMeeples()) {
			if (! (m.getFeature() instanceof City)) continue;
			if (m.getFeature().getRepresentativeFeature().equals(cityRepresentative) && m instanceof Follower) {
				princessAction.getOrCreate(m.getPosition()).add(m.getLocation());
			}
		}
		if (! princessAction.getSites().isEmpty()) {
			actions.add(princessAction);
		}
	}
	
	@Override
	public PrincessAndDragonGame copy() {
		PrincessAndDragonGame copy = new PrincessAndDragonGame();
		copy.game = game;		
		copy.dragonPosition = dragonPosition;
		copy.dragonMovesLeft = dragonMovesLeft;
		copy.dragonPlayer = dragonPlayer;
		if (dragonVisitedTiles != null) copy.dragonVisitedTiles = Sets.newHashSet(dragonVisitedTiles);
		copy.fairyPosition = fairyPosition;
		return copy;
	}
	

	@Override
	public void saveToSnapshot(Document doc, Element node) {
		if (dragonPosition != null) {
			Element dragon = doc.createElement("dragon");
			XmlUtils.injectPosition(dragon, dragonPosition);
			if (dragonMovesLeft > 0) {
				dragon.setAttribute("moves", "" + dragonMovesLeft);
				dragon.setAttribute("movingPlayer", "" + dragonPlayer);
				if (dragonVisitedTiles != null) {
					for(Position visited : dragonVisitedTiles) {
						Element ve = doc.createElement("visited");
						XmlUtils.injectPosition(ve, visited);
						dragon.appendChild(ve);
					}
				}
			}
			node.appendChild(dragon);
		}
		if (fairyPosition != null) {
			Element fairy = doc.createElement("fairy");
			XmlUtils.injectPosition(fairy, fairyPosition);
			node.appendChild(fairy);
		}
	}

	@Override
	public void loadFromSnapshot(Document doc, Element node) {
		NodeList nl = node.getElementsByTagName("dragon");
		if (nl.getLength() > 0) {
			Element dragon = (Element) nl.item(0);
			dragonPosition = XmlUtils.extractPosition(dragon);
			game.fireGameEvent().dragonMoved(dragonPosition);
			if (dragon.hasAttribute("moves")) {
				dragonMovesLeft  = Integer.parseInt(dragon.getAttribute("moves"));
				dragonPlayer = Integer.parseInt(dragon.getAttribute("movingPlayer"));
				dragonVisitedTiles = Sets.newHashSet();
				NodeList vl = dragon.getElementsByTagName("visited");
				for(int i = 0; i < vl.getLength(); i++) {
					dragonVisitedTiles.add(XmlUtils.extractPosition((Element) vl.item(i)));
				}
			}
		}
		nl = node.getElementsByTagName("fairy");
		if (nl.getLength() > 0) {
			Element fairy = (Element) nl.item(0);
			fairyPosition = XmlUtils.extractPosition(fairy);
			game.fireGameEvent().fairyMoved(fairyPosition);
		}
	}

}
