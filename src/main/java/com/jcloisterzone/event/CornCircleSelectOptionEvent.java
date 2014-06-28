package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

@Idempotent
public class CornCircleSelectOptionEvent extends PlayEvent {
	
	private final Position position;

    public CornCircleSelectOptionEvent(Player player, Position position) {
        super(player);
        this.position = position;
    }
    
    public Position getPosition() {
		return position;
	}

}
