package com.jcloisterzone.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;

public abstract class SelectFeatureAction extends PlayerAction<FeaturePointer> {


    public SelectFeatureAction(String name) {
        super(name);
    }


    @Override
    protected GridLayer createGridLayer() {
        return new FeatureAreaLayer(client.getGridPanel(), this);
    }
    
    public Map<Position, Set<Location>> groupByPosition() {
    	Map<Position, Set<Location>> map = new HashMap<>();
    	for (FeaturePointer fp: options) {
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

//    @Override
//    public String toString() {
//        return getClass().getSimpleName() + '=' + locMap.toString();
//    }

}
