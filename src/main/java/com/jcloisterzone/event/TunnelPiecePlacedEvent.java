package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;

public class TunnelPiecePlacedEvent extends PlayEvent {

    private boolean secondPiece;

    public TunnelPiecePlacedEvent(Player player, Position position, Location location, boolean secondPiece) {
        super(player, position, location);
        this.secondPiece = secondPiece;
    }

    public boolean isSecondPiece() {
        return secondPiece;
    }

}
