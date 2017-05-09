package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;

import io.vavr.collection.Set;

// TODO generic token action ?

@LinkedImage("actions/gold")
@LinkedGridLayer(TileActionLayer.class)
public class GoldPieceAction extends SelectTileAction {

    public GoldPieceAction(Set<Position> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, Position p) {
        //server.placeGoldPiece(p);
    	//TODO
    }

    @Override
    public String toString() {
        return "place gold piece";
    }

}
