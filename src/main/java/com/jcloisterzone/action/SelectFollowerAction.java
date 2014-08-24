package com.jcloisterzone.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.FollowerAreaLayer;


public abstract class SelectFollowerAction extends PlayerAction<MeeplePointer> {

    public SelectFollowerAction(String name) {
        super(name);
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return FollowerAreaLayer.class;
    }

    //temporary legacy, TODO direct meeple selection on client

    public Map<Position, Set<Location>> groupByPosition() {
        Map<Position, Set<Location>> map = new HashMap<>();
        for (MeeplePointer fp: options) {
            Set<Location> locations = map.get(fp.getPosition());
            if (locations == null) {
                locations = new HashSet<>();
                map.put(fp.getPosition(), locations);
            }
            locations.add(fp.getLocation());
        }
        return map;
    }

  //TODO direct implementation
    public Set<Location> getLocations(Position p) {
        return groupByPosition().get(p);
    }

}
