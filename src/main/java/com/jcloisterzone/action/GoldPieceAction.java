package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;


@LinkedImage("actions/gold")
@LinkedGridLayer(TileActionLayer.class)
public class GoldPieceAction extends SelectTileAction {

    public GoldPieceAction(Set<Position> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(Position pos) {
        return new PlaceTokenMessage(Token.GOLD, pos);
    }

    @Override
    public String toString() {
        return "place gold piece";
    }
}
