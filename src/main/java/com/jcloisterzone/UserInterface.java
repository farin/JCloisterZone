package com.jcloisterzone;

import java.util.EventListener;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;


public interface UserInterface extends EventListener {

	void selectAction(List<PlayerAction> actions, boolean canPass);
	
	void showWarning(String title, String message);
	
	//deprecated - use unified interface 	
	void selectDragonMove(Set<Position> positions, int movesLeft);
	
	

}
