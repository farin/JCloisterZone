package com.jcloisterzone.ai;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;

public class DummyAiPlayer extends AiPlayer {


	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		selectDummyAbbeyPlacement(positions);
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> placements) {
		selectDummyTilePlacement(placements);
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		selectDummyAction(actions);
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		selectDummyTowerCapture(action);
	}

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		selectDummyDragonMove(positions, movesLeft);
	}
	
}
