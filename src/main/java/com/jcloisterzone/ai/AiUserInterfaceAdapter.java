package com.jcloisterzone.ai;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;

/**
 * Dummy fallback prevent application freezing for some AiPlayers implementation errors 
 */
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
			try {
				aiPlayer.selectAbbeyPlacement(positions);
			} catch(Exception e) {	
				aiPlayer.handleRuntimeError(e);
				aiPlayer.selectDummyAbbeyPlacement(positions);
			}
		}
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> positions) {
		if (aiPlayer.isAiPlayerActive()) {
			try {
				aiPlayer.selectTilePlacement(positions);
			} catch (Exception e) {
				aiPlayer.handleRuntimeError(e);
				aiPlayer.selectDummyTilePlacement(positions);
			}
		}
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		if (aiPlayer.isAiPlayerActive()) {
			try {
				aiPlayer.selectAction(actions);
			} catch (Exception e) {
				aiPlayer.handleRuntimeError(e);
				aiPlayer.selectDummyAction(actions);
			}
		}
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		if (aiPlayer.isAiPlayerActive()) {
			try {
				aiPlayer.selectTowerCapture(action);
			} catch (Exception e) {
				aiPlayer.handleRuntimeError(e);
				aiPlayer.selectDummyTowerCapture(action);
			}
		}
	}

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		if (aiPlayer.isAiPlayerActive()) {
			try {
				aiPlayer.selectDragonMove(positions, movesLeft);
			} catch (Exception e) {
				aiPlayer.handleRuntimeError(e);
				aiPlayer.selectDummyDragonMove(positions, movesLeft);
			}
		}
	}
	
	@Override
	public void showWarning(String title, String message) {	
		aiPlayer.showWarning(title, message);
	}

}
