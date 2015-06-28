package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;

public class TunnelPiecePlacedEvent extends FeatureEvent {

    private boolean secondPiece;

    public TunnelPiecePlacedEvent(Player player, FeaturePointer fp, boolean secondPiece) {
        super(player, fp);
        this.secondPiece = secondPiece;
    }

    public boolean isSecondPiece() {
        return secondPiece;
    }

}
