package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.BarnAreaLayer;

//TODO do not extends select feature, use special type for corner based on position
public class BarnAction extends SelectFeatureAction {

    public BarnAction() {
        super("barn");
    }

    @Override
    public void perform(Client2ClientIF server, FeaturePointer bp) {
        server.deployMeeple(bp.getPosition(), bp.getLocation(), Barn.class);
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
