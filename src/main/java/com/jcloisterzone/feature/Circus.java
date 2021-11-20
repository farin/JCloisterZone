package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;


public class Circus extends TileFeature implements Structure {

    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Circus.class, Location.I));

    public Circus() {
        this(INITIAL_PLACE);
    }

    public Circus(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Circus(placeOnBoardPlaces(pos, rot));
    }

    public static String name() {
        return "Circus";
    }
}
