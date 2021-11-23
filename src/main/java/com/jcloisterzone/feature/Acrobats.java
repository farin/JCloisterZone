package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;

public class Acrobats extends TileFeature implements Structure {

    public static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Acrobats.class, Location.I));

    private Location direction;

    public Acrobats(List<FeaturePointer> places, Location direction) {
        super(places);
    	this.direction = direction;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Acrobats(placeOnBoardPlaces(pos, rot), direction);
    }

    public Location getDirection() {
        return direction;
    }
    
    public static String name() {
        return "Acrobats";
    }
}
