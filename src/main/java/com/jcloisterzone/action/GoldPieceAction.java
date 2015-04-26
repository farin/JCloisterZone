package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.wsio.RmiProxy;

public class GoldPieceAction extends SelectTileAction {

    public GoldPieceAction() {
        super("gold");
    }

    @Override
    public void perform(RmiProxy server, Position p) {
        server.placeGoldPiece(p);
    }

    @Override
    protected int getSortOrder() {
        return 30;
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return TileActionLayer.class;
    }

    @Override
    public String toString() {
        return "place gold piece";
    }

}
