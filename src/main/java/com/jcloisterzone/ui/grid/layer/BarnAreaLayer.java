package com.jcloisterzone.ui.grid.layer;

import java.awt.geom.Area;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;

public class BarnAreaLayer extends AbstractAreaLayer implements ActionLayer<BarnAction> {

    private BarnAction action;

    public BarnAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    protected Map<Location, Area> prepareAreas(Tile tile, Position p) {
        //quick fix
        if (getGame().getCurrentTile().getPosition().equals(p)) {
            Set<Location> locations = action.getLocations(p);
            return getClient().getResourceManager().getBarnTileAreas(tile, getSquareSize(), locations);
        }
        return null;
    }

    @Override
    protected void performAction(Position pos, Location selected) {
        action.perform(getRmiProxy(), new FeaturePointer(pos, selected));
    }

    @Override
    public void setAction(boolean active, BarnAction action) {
        this.action = action;
        setActive(active);
    }

    @Override
    public BarnAction getAction() {
        return action;
    }

}
