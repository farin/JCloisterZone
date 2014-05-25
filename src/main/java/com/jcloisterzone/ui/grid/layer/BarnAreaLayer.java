package com.jcloisterzone.ui.grid.layer;

import java.awt.geom.Area;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.grid.GridPanel;

public class BarnAreaLayer extends AbstractAreaLayer {

    final BarnAction action;

    public BarnAreaLayer(GridPanel gridPanel, BarnAction action) {
        super(gridPanel);
        this.action = action;
    }

    @Override
    protected Map<Location, Area> prepareAreas(Tile tile, Position p) {
        //quick fix
        if (getClient().getGame().getCurrentTile().getPosition().equals(p)) {
        	Set<Location> locations = action.getLocations(p);
            return getClient().getResourceManager().getBarnTileAreas(tile, getSquareSize(), locations);
        }
        return null;
    }

    @Override
    protected void performAction(Position pos, Location selected) {
        action.perform(getClient().getServer(), new FeaturePointer(pos, selected));
    }

}
