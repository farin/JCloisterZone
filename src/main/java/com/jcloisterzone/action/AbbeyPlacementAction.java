package com.jcloisterzone.action;

import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.AbbeyPlacementLayer;
import com.jcloisterzone.wsio.RmiProxy;

public class AbbeyPlacementAction extends SelectTileAction {

    public AbbeyPlacementAction() {
        super("abbeyplacement");
    }

    @Override
    public Image getImage(Player player, boolean active) {
        return client.getResourceManager().getAbbeyImage();
    }

    @Override
    public void perform(RmiProxy server, Position p) {
        server.placeTile(Rotation.R0, p);
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return AbbeyPlacementLayer.class;
    }

    @Override
    public String toString() {
        return "place abbey";
    }

}
