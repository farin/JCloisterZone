package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.BarnAreaLayer;


public class BarnAction extends SelectFeatureAction {

    public BarnAction() {
        super("barn");
    }

    public void perform(Client2ClientIF server, Position p, Location d) {
        server.deployMeeple(p, d, Barn.class);
    }

    @Override
    protected GridLayer createGridLayer() {
        return new BarnAreaLayer(client.getGridPanel(), this);
    }

    @Override
    protected int getSortOrder() {
        return 9;
    }
}
