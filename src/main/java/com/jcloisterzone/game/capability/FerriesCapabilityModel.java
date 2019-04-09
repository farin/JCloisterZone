package com.jcloisterzone.game.capability;

import java.io.Serializable;
import java.util.function.Function;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

@Immutable
public class FerriesCapabilityModel implements Serializable {

	private static final long serialVersionUID = 1L;

    /** all placed ferries */
    private final Set<FeaturePointer> ferries;

    /** ferries moved this turn */
    private final Map<Position, Tuple2<Location, Location>> movedFerries;

    public FerriesCapabilityModel() {
       this(HashSet.empty(), HashMap.empty());
    }

    public FerriesCapabilityModel(Set<FeaturePointer> ferries, Map<Position, Tuple2<Location, Location>> movedFerries) {
        this.ferries = ferries;
        this.movedFerries = movedFerries;
    }

    public Set<FeaturePointer> getFerries() {
        return ferries;
    }

    public Map<Position, Tuple2<Location, Location>> getMovedFerries() {
        return movedFerries;
    }

    public FerriesCapabilityModel addFerry(FeaturePointer ferry) {
        return new FerriesCapabilityModel(ferries.add(ferry), movedFerries);
    }

    public FerriesCapabilityModel setMovedFerries(Map<Position, Tuple2<Location, Location>> movedFerries) {
        if (this.movedFerries == movedFerries) return this;
        return new FerriesCapabilityModel(ferries, movedFerries);
    }

    public FerriesCapabilityModel mapMovedFerries(Function<Map<Position, Tuple2<Location, Location>>, Map<Position, Tuple2<Location, Location>>> fn) {
        return setMovedFerries(fn.apply(movedFerries));
    }
}
