package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;


public class Tower extends TileFeature {

    private final int height;

    public Tower(List<FeaturePointer> places) {
        this(places, 0);
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
        return _("Tower");
    }
}
