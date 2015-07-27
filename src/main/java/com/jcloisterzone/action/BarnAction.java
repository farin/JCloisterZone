package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.BarnAreaLayer;
import com.jcloisterzone.wsio.RmiProxy;

//TODO do not extends select feature, use special type for corner based on position
public class BarnAction extends SelectFeatureAction {

    public BarnAction() {
        super("barn");
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployMeeple(bp, Barn.class);
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return BarnAreaLayer.class;
    }

    @Override
    protected int getSortOrder() {
        return 11;
    }

    @Override
    public String toString() {
        return "place barn";
    }
}
