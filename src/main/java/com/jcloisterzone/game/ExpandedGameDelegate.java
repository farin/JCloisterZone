package com.jcloisterzone.game;

import java.util.List;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;


public class ExpandedGameDelegate implements GameDelegation {

	private final Game game;

	public ExpandedGameDelegate(Game game) {
		this.game = game;
	}

	@Override
	public void initTile(Tile tile, Element xml) {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.initTile(tile, xml);
		}
	}

	@Override
	public void initFeature(Tile tile, Feature feature, Element xml) {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.initFeature(tile, feature, xml);
		}
	}

	@Override
	public void initPlayer(Player player) {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.initPlayer(player);
		}
	}

	@Override
	public void begin() {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.begin();
		}
	}

	@Override
	public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.prepareActions(actions, commonSites);
		}
	}

	@Override
	public void scoreCompleted(CompletableScoreContext ctx) {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.scoreCompleted(ctx);
		}
	}

	@Override
	public void turnCleanUp() {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.turnCleanUp();
		}

	}

	@Override
	public void finalScoring() {
		for(GameDelegation eg: game.getExpandedGames()) {
			eg.finalScoring();
		}
	}


	@Override
	public boolean checkMove(Tile tile, Position p) {
		for(GameDelegation eg: game.getExpandedGames()) {
			if (! eg.checkMove(tile, p)) return false;
		}
		return true;
	}



}
