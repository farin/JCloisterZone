package com.jcloisterzone.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;

public abstract class RankingAiPlayer extends AiPlayer {

	class PositionLocation {
		Position position;
		Location location;
	}

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private Deque<GameBackup> backups = new ArrayDeque<GameBackup>();
	private UserInterface gameCopyUiAdapter = new GameCopyUserInerfaceAdapter(this);

	private PositionRanking bestSoFar;
	private List<PositionLocation> hopefulGatePlacements = new ArrayList<PositionLocation>();

	protected void backup() {
		backups.push(new GameBackup(getGame()));
	}

	protected void copy() {
		Game game = backups.peek().copy();
		game.addUserInterface(gameCopyUiAdapter);
		setGame(game);
	}

	protected void restore() {
		setGame(backups.pop().getGame());
	}

	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> placements) {
		bestSoFar = new PositionRanking(Double.NEGATIVE_INFINITY);
		backup();
		for(Entry<Position, Set<Rotation>> entry : placements.entrySet()) {
			Position pos = entry.getKey();
			for(Rotation rot : entry.getValue()) {
				copy();
				getGame().getPhase().placeTile(rot, pos);
				double currRank = rank();
				if (currRank > bestSoFar.getRank()) {
					bestSoFar = new PositionRanking(currRank, pos, rot);
				}
				//now rank meeple placements - must restore because rank change game
				copy();
				getGame().getPhase().placeTile(rot, pos);
				hopefulGatePlacements.clear();
				//TODO add best placements for MAGIC GATE
				getGame().getPhase().enter();
			}
		}
		restore();
		logger.info("Selected move is: {}", bestSoFar);
		getServer().placeTile(bestSoFar.getRotation(), bestSoFar.getPosition());
	}

	public void rankAction(List<PlayerAction> actions) {
		backup();

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
		restore();
	}

	public void rankMeepleAction(Tile currTile, MeepleAction meepleAction, Position pos, Set<Location> locations) {
		if (locations == null) {
			return;
		}
		for(Location loc : locations) {
			copy();
			getGame().getPhase().deployMeeple(pos, loc, meepleAction.getMeepleType());
			double currRank = rank();
			if (currRank > bestSoFar.getRank()) {
				bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
				bestSoFar.setAction(meepleAction);
				bestSoFar.setActionPosition(pos);
				bestSoFar.setActionLocation(loc);
			}
		}
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
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
