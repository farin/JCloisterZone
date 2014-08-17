package com.jcloisterzone.action;

import java.awt.Image;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.RmiProxy;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;

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
    protected GridLayer createGridLayer() {
        Image gd = client.getControlsTheme().getActionDecoration("fairy");
        return new TileActionLayer(client.getGridPanel(), this, gd);
    }

    @Override
    public String toString() {
        return "move fairy";
    }

}
