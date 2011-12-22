package com.jcloisterzone.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ai.copy.CopyGamePhase;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.Phase;

public abstract class RankingAiPlayer extends AiPlayer {

	enum GameState { PLAY, RANK; };

	class PositionLocation {
		Position position;
		Location location;
	}

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	//private Deque<SavePointManager> backups = new ArrayDeque<SavePointManager>();
	private SavePointManager spm;
//	private UserInterface gameCopyUiAdapter = new GameCopyUserInerfaceAdapter(this);

	private PositionRanking bestSoFar;
	private List<PositionLocation> hopefulGatePlacements = new ArrayList<PositionLocation>();

	private GameState gameState = GameState.PLAY;

//	protected void backup() {
//		backups.push(new SavePointManager(getGame()));
//	}
//
//	protected void copy() {
//		Game game = backups.peek().copy();
//		game.addUserInterface(gameCopyUiAdapter);
//		setGame(game);
//	}
//
//	protected void restore() {
//		setGame(backups.pop().getGame());
//	}

	//TODO copy game for AI purposes ???

	@Override
	public void setGame(Game game) {
		super.setGame(game);
		//spm = new SavePointManager(game);
	}

	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		throw new UnsupportedOperationException();
	}

	/* TEMPORARY COPIED FROM CLIENT STUB */
	@Deprecated
	private void phaseLoop() {
		Phase phase = getGame().getPhase();
		while(! phase.isEntered()) {
			//logger.info("  * not entered {} -> {}", phase, phase.getDefaultNext());
			phase.setEntered(true);
			phase.enter();
			//phase = getGame().getPhase();
			break; //now only transition Tile -> Action phase
		}
	}
	
	//TEMPORARY method
	//TODO fast game copying without snapshot ?
	//TODO do not recreate SavePointManager
	private void copyGame() {
		Snapshot snapshot = new Snapshot(getGame(), 0); 
		Game gameCopy = snapshot.asGame();		
		gameCopy.setConfig(getGame().getConfig());
		gameCopy.addGameListener(new GameEventAdapter());
		gameCopy.addUserInterface(this);
		Phase phase = new CopyGamePhase(gameCopy, snapshot, getGame().getTilePack());
		gameCopy.getPhases().put(phase.getClass(), phase);
		gameCopy.setPhase(phase);
		phase.startGame();
		setGame(gameCopy);
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> placements) {
		logger.info("---------- Ranking start ---------------");
		logger.info("Positions: {} ", placements.keySet());
		
		Game original = getGame();
		copyGame();
		spm = new SavePointManager(getGame());
		gameState = GameState.RANK;
		bestSoFar = new PositionRanking(Double.NEGATIVE_INFINITY);
		spm.startRecording();
		SavePoint sp = spm.save();
		for(Entry<Position, Set<Rotation>> entry : placements.entrySet()) {
			Position pos = entry.getKey();
			for(Rotation rot : entry.getValue()) {
				//logger.info("  * phase {} -> {}", getGame().getPhase(), getGame().getPhase().getDefaultNext());
				logger.info("  * placing {} {}", pos, rot);
				getGame().getPhase().placeTile(rot, pos);
				//logger.info("  * phase {} -> {}", getGame().getPhase(), getGame().getPhase().getDefaultNext());
				phaseLoop();
				double currRank = rank();
				if (currRank > bestSoFar.getRank()) {
					bestSoFar = new PositionRanking(currRank, pos, rot);
				}
				spm.restore(sp);
				//TODO fix hopefulGatePlacement
				//now rank meeple placements - must restore because rank change game
				//getGame().getPhase().placeTile(rot, pos);
				hopefulGatePlacements.clear();
				spm.restore(sp);
				//TODO add best placements for MAGIC GATE
				//getGame().getPhase().enter();
			}
		}
		spm.stopRecording();
		gameState = GameState.PLAY;
		setGame(original);
		spm = null;
		logger.info("Selected move is: {}", bestSoFar);
		getServer().placeTile(bestSoFar.getRotation(), bestSoFar.getPosition());
	}

	public void rankAction(List<PlayerAction> actions) {
		for(PlayerAction action : actions) {
			if (action instanceof MeepleAction) {
				MeepleAction ma = (MeepleAction) action;
				Tile currTile = getGame().getTilePack().getCurrentTile();
				Position pos = currTile.getPosition();
				rankMeepleAction(currTile, ma, pos, ma.getSites().get(pos));
				for(PositionLocation posloc : hopefulGatePlacements) {
					rankMeepleAction(currTile, ma, posloc.position, Collections.singleton(posloc.location));
				}
			}
		}
	}

	public void rankMeepleAction(Tile currTile, MeepleAction meepleAction, Position pos, Set<Location> locations) {
		if (locations == null) {
			return;
		}
		SavePoint sp = spm.save();
		for(Location loc : locations) {
			logger.info("    . deploying {}", meepleAction.getMeepleType());
			getGame().getPhase().deployMeeple(pos, loc, meepleAction.getMeepleType());
			double currRank = rank();
			if (currRank > bestSoFar.getRank()) {
				bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
				bestSoFar.setAction(meepleAction);
				bestSoFar.setActionPosition(pos);
				bestSoFar.setActionLocation(loc);
			}
			spm.restore(sp);
		}
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		if (gameState == GameState.RANK) {
			rankAction(actions);
			return;
		}

		if (bestSoFar.getAction() instanceof MeepleAction) {
			MeepleAction action = (MeepleAction) bestSoFar.getAction();
			action.perform(getServer(), bestSoFar.getActionPosition(), bestSoFar.getActionLocation());
			return;
		}
		getServer().placeNoFigure();
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		throw new UnsupportedOperationException();
	}

	abstract protected double rank();

}
