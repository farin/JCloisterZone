package com.jcloisterzone;

import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;


public interface UserInterface extends EventListener {

	void selectAction(List<PlayerAction> actions, boolean canPass);
	
	void showWarning(String title, String message);
	
	//deprecated - use unified interface 
	void selectAbbeyPlacement(Set<Position> positions);
	//void selectTilePlacement(Map<Position, Set<Rotation>> placements);	
	void selectDragonMove(Set<Position> positions, int movesLeft);
	
	

}
