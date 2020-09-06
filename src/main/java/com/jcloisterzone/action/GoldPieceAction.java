package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.capability.GoldminesCapability.GoldToken;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.Set;

public class GoldPieceAction extends SelectTileAction {

    public GoldPieceAction(Set<Position> options) {
        super(options);
    }
}

