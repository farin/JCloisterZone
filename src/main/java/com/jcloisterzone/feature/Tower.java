package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;


public class Tower extends TileFeature implements Structure {

    private final int height;
    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Tower.class, Location.I));

    public Tower() {
        this(INITIAL_PLACE, 0);
    }

    public Tower(List<FeaturePointer> places, int height) {
        super(places);
        this.height = height;
    }

    @Override
    public Tower placeOnBoard(Position pos, Rotation rot) {
        return new Tower(placeOnBoardPlaces(pos, rot), height);
    }

    public Tower increaseHeight() {
        return new Tower(places, height + 1);
    }

    public int getHeight() {
        return height;
    }

    public static String name() {
        return "Tower";
    }
}
