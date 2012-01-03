package com.jcloisterzone.ai;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;

public class DummyAiPlayer extends AiPlayer {


	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		getServer().placeNoTile();
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> placements) {
		Position nearest = null, p0 = new Position(0, 0);
		int min = Integer.MAX_VALUE;
		for(Position pos : placements.keySet()) {
			int dist = pos.squareDistance(p0);
			if (dist < min) {
				min = dist;
				nearest = pos;
			}
		}
		getServer().placeTile(placements.get(nearest).iterator().next(), nearest);
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		for(PlayerAction action : actions) {
			if (action instanceof MeepleAction) {
				MeepleAction ma = (MeepleAction) action;
				Position p = ma.getSites().keySet().iterator().next();
				for(Location loc : ma.getSites().get(p)) {
					Feature f = getBoard().get(p).getFeature(loc);
					if (f instanceof City || f instanceof Road || f instanceof Cloister) {
						getServer().deployMeeple(p, loc, ma.getMeepleType());
						return;
					}
				}
			}
		}
		getServer().placeNoFigure();
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		Position p = action.getSites().keySet().iterator().next();
		Location loc = action.getSites().get(p).iterator().next();
		getServer().captureFigure(p, loc);
	}

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		getServer().moveDragon(positions.iterator().next());
	}
	
}
