package com.jcloisterzone.ai;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;

public class AiUserInterfaceAdapter implements UserInterface {

	private AiPlayer aiPlayer;

	public AiUserInterfaceAdapter(AiPlayer aiPlayer) {
		this.aiPlayer = aiPlayer;
	}

	public AiPlayer getAiPlayer() {
		return aiPlayer;
	}

	public void setAiPlayer(AiPlayer aiPlayer) {
		this.aiPlayer = aiPlayer;
	}

	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		if (aiPlayer.isAiPlayerActive()) {
			aiPlayer.selectAbbeyPlacement(positions);
		}
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> positions) {
		if (aiPlayer.isAiPlayerActive()) {
			aiPlayer.selectTilePlacement(positions);
		}
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		if (aiPlayer.isAiPlayerActive()) {
			aiPlayer.selectAction(actions);
		}
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		if (aiPlayer.isAiPlayerActive()) {
			aiPlayer.selectTowerCapture(action);
		}
	}

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		if (aiPlayer.isAiPlayerActive()) {
			aiPlayer.selectDragonMove(positions, movesLeft);
		}
	}
	
	@Override
	public void selectBridgePlacement(BridgeAction action) {
		if (aiPlayer.isAiPlayerActive()) {
			aiPlayer.selectBridgePlacement(action);
		}		
	}

}
