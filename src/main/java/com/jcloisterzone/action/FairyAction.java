package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.wsio.RmiProxy;

public class FairyAction extends SelectTileAction {

    public FairyAction() {
        super("fairy");
    }

    @Override
    public void perform(RmiProxy server, Position p) {
        server.moveFairy(p);
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
        return "move fairy";
    }

}
