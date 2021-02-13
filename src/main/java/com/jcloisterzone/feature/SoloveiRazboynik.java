package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;

public class SoloveiRazboynik extends TileFeature implements Structure {

    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.TOWER));

    public SoloveiRazboynik() {
        this(INITIAL_PLACE);
    }

    public SoloveiRazboynik(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public SoloveiRazboynik placeOnBoard(Position pos, Rotation rot) {
        return new SoloveiRazboynik(placeOnBoardPlaces(pos, rot));
    }

    public static String name() {
        return "SoloveiRazboynik";
    }
}
