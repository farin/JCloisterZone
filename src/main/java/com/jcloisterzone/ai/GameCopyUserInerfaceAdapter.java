package com.jcloisterzone.ai;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;

@Deprecated //TODO DELETE
class GameCopyUserInerfaceAdapter implements UserInterface {

	private final RankingAiPlayer rankingAiPlayer;

	GameCopyUserInerfaceAdapter(RankingAiPlayer rankingAiPlayer) {
		this.rankingAiPlayer = rankingAiPlayer;
	}

	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> placements) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		this.rankingAiPlayer.rankAction(actions);
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		throw new UnsupportedOperationException();
	}

}