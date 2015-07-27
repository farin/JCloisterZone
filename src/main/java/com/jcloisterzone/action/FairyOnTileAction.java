package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.wsio.RmiProxy;

public class FairyOnTileAction extends SelectTileAction {

    public FairyOnTileAction() {
        super("fairy");
    }

    @Override
    public void perform(RmiProxy server, Position p) {
        server.moveNeutralFigure(p.asFeaturePointer(), Fairy.class);
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
