package com.jcloisterzone.game.phase;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.rmi.ClientIF;


public abstract class Phase implements ClientIF {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected final Game game;

	private boolean entered;
	private Phase defaultNext;

	public Phase(Game game) {
		this.game = game;
	}

	public boolean isEntered() {
		return entered;
	}

	public void setEntered(boolean entered) {
		this.entered = entered;
	}

	public Phase getDefaultNext() {
		return defaultNext;
	}

	public void setDefaultNext(Phase defaultNext) {
		this.defaultNext = defaultNext;
	}

	public void next() {
		game.setPhase(defaultNext);
	}

	public void next(Class<? extends Phase> phaseClass) {
		game.setPhase(game.getPhases().get(phaseClass));
	}

	public void enter() { }

	public boolean isActive() {
		return true;
	}

	//shortcuts

	protected TilePack getTilePack() {
		return game.getTilePack();
	}
	protected Board getBoard() {
		return game.getBoard();
	}
	@Deprecated
	protected Game getGame() {
		return game;
	}
	protected Tile getTile() {
		return game.getTilePack().getCurrentTile();
	}

	public Player getActivePlayer() {
		return game.getTurnPlayer();
	}

	//adapter methods

	@Override
	public void startGame() {
		logger.error(Application.ILLEGAL_STATE_MSG, "startGame");
	}

	@Override
	public void placeNoFigure() {
		logger.error(Application.ILLEGAL_STATE_MSG, "placeNoFigure");
	}

	@Override
	public void placeNoTile() {
		logger.error(Application.ILLEGAL_STATE_MSG, "placeNoTile");
	}

	@Override
	public void placeTile(Rotation  rotation, Position position) {
		logger.error(Application.ILLEGAL_STATE_MSG, "placeTile");
	}

	@Override
	public void deployMeeple(Position p,  Location d, Class<? extends Meeple> meepleType) {
		logger.error(Application.ILLEGAL_STATE_MSG, "placeFigure");
	}

	@Override
	public void moveFairy(Position p) {
		logger.error(Application.ILLEGAL_STATE_MSG, "placeFairy");
	}

	@Override
	public void placeTowerPiece(Position p) {
		logger.error(Application.ILLEGAL_STATE_MSG, "placeTowerPiece");
	}

	@Override
	public void placeTunnelPiece(Position p, Location d, boolean isSecondPiece) {
		logger.error(Application.ILLEGAL_STATE_MSG, "placeTunnelPiece");
	}

	@Override
	public void removeKnightWithPrincess(Position p, Location d) {
		logger.error(Application.ILLEGAL_STATE_MSG, "kickFigureByPrincess");
	}

	@Override
	public void moveDragon(Position p) {
		logger.error(Application.ILLEGAL_STATE_MSG, "moveDragon");
	}

	@Override
	public void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType) {
		//pay ransom is valid anytime
		game.getTowerGame().payRansom(playerIndexToPay, meepleType);
	}

	@Override
	public void updateCustomRule(CustomRule rule, Boolean enabled) {
		logger.error(Application.ILLEGAL_STATE_MSG, "updateCustomRule");
	}
	@Override
	public void updateExpansion(Expansion expansion, Boolean enabled) {
		logger.error(Application.ILLEGAL_STATE_MSG, "updateExpansion");

	}
	@Override
	public void updateSlot(PlayerSlot slot) {
		logger.error(Application.ILLEGAL_STATE_MSG, "updateSlot");
	}

	@Override
	public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
		logger.error(Application.ILLEGAL_STATE_MSG, "updateSupportedExpansions");
	}


	@Override
	public void nextTile(Integer tileIndex) {
		logger.error(Application.ILLEGAL_STATE_MSG, "nextTile");
	}

	@Override
	public void escapeFromCity(Position p, Location loc) {
		logger.error(Application.ILLEGAL_STATE_MSG, "escapeFromCity");
	}

	 @Override
	public void captureFigure(Position p, Location d) {
		 logger.error(Application.ILLEGAL_STATE_MSG, "captureFigure");
	}
	 
	@Override
	public void deployBridge(Position pos, Location loc) {
		logger.error(Application.ILLEGAL_STATE_MSG, "deployBridge");
		
	}


}
