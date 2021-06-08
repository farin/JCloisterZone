package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

/**
 * Represents a flying machine from The Flier expansion.
 */
public class FlyingMachine implements Structure {

    private FeaturePointer place;
    private Location direction;

    public FlyingMachine(FeaturePointer place, Location direction) {
        this.place = place;
        this.direction = direction;
    }

    @Override
    public List<FeaturePointer> getPlaces() {
        return List.of(place);
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new FlyingMachine(place.translate(pos), direction.rotateCW(rot));
    }

    @Override
    public Stream<Tuple2<Meeple, FeaturePointer>> getMeeples2(GameState state) {
        return Stream.empty();
    }

    @Override
    public boolean isOccupied(GameState state) {
        return false;
    }

    public Location getDirection() {
        return direction;
    }

}
