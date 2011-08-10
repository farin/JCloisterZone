package com.jcloisterzone.collection;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;

//TODO extends from map of sets
public class Sites extends HashMap<Position, Set<Location>> {

	private static final long serialVersionUID = -3854304371401326525L;

	public Set<Location> getOrCreate(Position p) {
		Set<Location> locations = get(p);
		if (locations == null) {
			locations = Sets.newHashSet();
			put(p, locations);
		}
		return locations;
	}

}
