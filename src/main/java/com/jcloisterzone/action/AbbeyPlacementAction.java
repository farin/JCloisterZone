package com.jcloisterzone.action;

import java.awt.Image;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.AbbeyPlacementLayer;

public class AbbeyPlacementAction extends SelectTileAction {

    public AbbeyPlacementAction(Set<Position> sites) {
        super("abbeyplacement", sites);
    }

    @Override
    public Image getImage(Player player, boolean active) {
        return client.getResourceManager().getAbbeyImage();
    }

    @Override
    public void perform(Client2ClientIF server, Position p) {
        server.placeTile(Rotation.R0, p);
    }

    @Override
    protected GridLayer createGridLayer() {
        return new AbbeyPlacementLayer(client.getGridPanel(), this);
    }

}
