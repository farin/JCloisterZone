package com.jcloisterzone.game.expansion;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.ExpandedGame;
import com.jcloisterzone.game.Game;


public final class TradersAndBuildersGame extends ExpandedGame {

	public enum BuilderState { INACTIVE, ACTIVATED, BUILDER_TURN; }

	protected BuilderState builderState = BuilderState.INACTIVE;

	protected Map<Player,int[]> tradeResources = Maps.newHashMap();

	@Override
	public void initPlayer(Player player) {
		player.addMeeple(new Builder(game, player));
		player.addMeeple(new Pig(game, player));
		tradeResources.put(player, new int[TradeResource.values().length]);
	}

	@Override
	public void setGame(Game game) {
		super.setGame(game);
		game.addGameListener(new GameEventAdapter() {
			@Override
			public void completed(Completable feature, CompletableScoreContext ctx) {
				if (feature instanceof City) {
					int cityTradeResources[] = ((CityScoreContext)ctx).getCityTradeResources();
					if (cityTradeResources != null) {
						int playersTradeResources[] = tradeResources.get(getGame().getActivePlayer());
						for(int i = 0; i < cityTradeResources.length; i++) {
							playersTradeResources[i] += cityTradeResources[i];
						}
					}
				}
			}
		});
	}

	public void addTradeResources(Player p, TradeResource res, int n) {
		tradeResources.get(p)[res.ordinal()] += n;
	}

	public int getTradeResources(Player p, TradeResource res) {
		return tradeResources.get(p)[res.ordinal()];
	}

	public BuilderState getBuilderState() {
		return builderState;
	}

	public void builderUsed() {
		if (builderState == BuilderState.INACTIVE) {
			builderState = BuilderState.ACTIVATED;
		}
	}

	public boolean takeAnotherTurn() {
		return builderState == BuilderState.ACTIVATED;
	}

	@Override
	public void initFeature(Tile tile, Feature feature, Element xml) {
		if (feature instanceof City && xml.hasAttribute("resource")) {
			City city = (City) feature;
			String val = xml.getAttribute("resource");
			city.setTradeResource(TradeResource.valueOf(val.toUpperCase()));
		}
	}

	@Override
	public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
		if (getTile().getTrigger() == TileTrigger.VOLCANO &&
			getGame().hasRule(CustomRule.CANNOT_PLACE_BUILDER_ON_VOLCANO)) return;

		Player player = game.getActivePlayer();
		Position pos = getTile().getPosition();
		if (player.hasSpeialMeeple(Builder.class)) {
			MeepleAction meepleAction = new MeepleAction(Builder.class);
			Set<Location> dirs = getTile().getPlayerFeatures(player, Road.class);
			if (! dirs.isEmpty()) meepleAction.getOrCreate(pos).addAll(dirs);
			dirs = getTile().getPlayerFeatures(player, City.class);
			if (! dirs.isEmpty()) meepleAction.getOrCreate(pos).addAll(dirs);
			if (! meepleAction.getSites().isEmpty()) actions.add(meepleAction);
		}
		if (player.hasSpeialMeeple(Pig.class)) {
			MeepleAction meepleAction = new MeepleAction(Pig.class);
			Set<Location> dirs = getTile().getPlayerFeatures(player, Farm.class);
			if (! dirs.isEmpty()) meepleAction.getOrCreate(pos).addAll(dirs);
			if (! meepleAction.getSites().isEmpty()) actions.add(meepleAction);
		}
	}

	@Override
	public void finalScoring() {
		for(TradeResource tr : TradeResource.values()) {
			int hiVal = 1;
			for(Player player: getGame().getAllPlayers()) {
				int playerValue = getTradeResources(player, tr);
				if (playerValue > hiVal) {
					hiVal = playerValue;
				}
			}
			for(Player player: getGame().getAllPlayers()) {
				int playerValue = getTradeResources(player, tr);
				if (playerValue == hiVal) {
					player.addPoints(10, PointCategory.TRADE_GOODS);
				}
			}

		}
	}

	@Override
	public void turnCleanUp() {
		if (builderState == BuilderState.ACTIVATED) {
			builderState = BuilderState.BUILDER_TURN;
			return;
		}
		if (builderState == BuilderState.BUILDER_TURN) {
			builderState = BuilderState.INACTIVE;
		}

	}

	@Override
	public void saveToSnapshot(Document doc, Element node) {
		for(Player player: game.getAllPlayers()) {
			Element el = doc.createElement("player");
			node.appendChild(el);
			el.setAttribute("index", "" + player.getIndex());
			el.setAttribute("grain", "" + getTradeResources(player, TradeResource.GRAIN));
			el.setAttribute("wine", "" + getTradeResources(player, TradeResource.WINE));
			el.setAttribute("cloth", "" + getTradeResources(player, TradeResource.CLOTH));
		}
	}

	@Override
	public void loadFromSnapshot(Document doc, Element node) {
		NodeList nl = node.getElementsByTagName("player");
		for(int i = 0; i < nl.getLength(); i++) {
			Element playerEl = (Element) nl.item(i);
			Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
			addTradeResources(player, TradeResource.GRAIN, Integer.parseInt(playerEl.getAttribute("grain")));
			addTradeResources(player, TradeResource.WINE, Integer.parseInt(playerEl.getAttribute("wine")));
			addTradeResources(player, TradeResource.CLOTH, Integer.parseInt(playerEl.getAttribute("cloth")));
		}

	}
}

