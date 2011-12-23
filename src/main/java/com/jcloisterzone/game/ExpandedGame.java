package com.jcloisterzone.game;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;


public class ExpandedGame implements GameDelegation {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected Game game;

	public Game getGame() {
		return game;
	}
	public void setGame(Game game) {
		this.game = game;
	}
	protected TilePack getTilePack() {
		return game.getTilePack();
	}
	protected Board getBoard() {
		return game.getBoard();
	}
	protected Tile getTile() {
		return getTilePack().getCurrentTile();
	}
	
	/** 
	 * @return object copy or null if expansion is stateless
	 */
	public ExpandedGame copy() {
		return null; 
	}

	public void saveToSnapshot(Document doc, Element node) {
	}

	public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
	}

	//--- delegation adapter methods ---

	@Override
	public void initTile(Tile tile, Element xml) {
	}


	@Override
	public void initFeature(Tile tile, Feature feature, Element xml) {
	}


	@Override
	public void initPlayer(Player player) {
	}

	@Override
	public void begin() {
	}

	@Override
	public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
	}

	@Override
	public void scoreCompleted(CompletableScoreContext ctx) {
	}

	@Override
	public void turnCleanUp() {

	}

	@Override
	public void finalScoring() {
	}


	@Override
	public boolean checkMove(Tile tile, Position p) {
		return true;
	}





}
