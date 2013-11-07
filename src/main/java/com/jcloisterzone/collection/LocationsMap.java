package com.jcloisterzone.collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;

public class LocationsMap extends HashMap<Position, Set<Location>> {

    private static final long serialVersionUID = -3854304371401326525L;

    public Set<Location> getOrCreate(Position p) {
        Set<Location> locations = get(p);
        if (locations == null) {
            locations = new HashSet<>();
            put(p, locations);
        }
        return locations;
    }

}
