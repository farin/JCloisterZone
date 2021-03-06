package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;

public class Vodyanoy extends TileFeature implements Scoreable, TrapFeature {

    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.TOWER));

    public Vodyanoy() {
        this(INITIAL_PLACE);
    }

    public Vodyanoy(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public Vodyanoy placeOnBoard(Position pos, Rotation rot) {
        return new Vodyanoy(placeOnBoardPlaces(pos, rot));
    }

    public static String name() {
        return "Vodyanoy";
    }
}
