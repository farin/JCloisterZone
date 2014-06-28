package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;

public class TunnelPiecePlacedEvent extends FeatureEvent {

    private boolean secondPiece;

    public TunnelPiecePlacedEvent(Player player, Position position, Location location, boolean secondPiece) {
        super(player, new FeaturePointer(position, location));
        this.secondPiece = secondPiece;
    }

    public boolean isSecondPiece() {
        return secondPiece;
    }

}
