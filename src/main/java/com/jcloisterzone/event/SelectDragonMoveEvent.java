package com.jcloisterzone.event;

import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

@Idempotent
public class SelectDragonMoveEvent extends PlayEvent {

    private final Set<Position> positions;
    private final int movesLeft;

    public SelectDragonMoveEvent(Player player, Set<Position> positions, int movesLeft) {
        super(player);
        this.positions = positions;
        this.movesLeft = movesLeft;
    }

    public Set<Position> getPositions() {
        return positions;
    }

    public int getMovesLeft() {
        return movesLeft;
    }


}
