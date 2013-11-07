package com.jcloisterzone.action;

import java.awt.Image;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;

public class FairyAction extends SelectTileAction {

    public FairyAction() {
        super("fairy");
    }

    @Override
    public void perform(Client2ClientIF server, Position p) {
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

}
