package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;

public class Acrobats extends TileFeature implements Structure {

    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Acrobats.class, Location.I));

    public Acrobats() {
        this(INITIAL_PLACE);
    }

    public Acrobats(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Acrobats(placeOnBoardPlaces(pos, rot));
    }

    public static String name() {
        return "Acrobats";
    }
}
