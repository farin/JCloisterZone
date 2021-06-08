package com.jcloisterzone.game.state;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.*;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import io.vavr.Tuple2;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Represents a tile that is placed on the map. This is made of a {@link Tile}, a {@link Position} and an
 * {@link Rotation}.
 */
@Immutable
public class PlacedTile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Tile tile;
    private final Position position;
    private final Rotation rotation;

    /**
     * Instantiates a new {@code PlacedTile}.
     *
     * @param tile     the tile
     * @param position the position
     * @param rotation the rotation
     */
    public PlacedTile(Tile tile, Position position, Rotation rotation) {
        this.tile = tile;
        this.position = position;
        this.rotation = rotation;
    }

    /**
     * Gets the tile data.
     *
     * @return the tile data
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * Gets the placement position.
     *
     * @return the position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets the placement rotation.
     *
     * @return the rotation
     */
    public Rotation getRotation() {
        return rotation;
    }

    /**
     * Sets the tile data.
     *
     * @param tile the tile
     * @return a new instance with the tile data updated
     */
    public PlacedTile setTile(Tile tile) {
        return new PlacedTile(tile, position, rotation);
    }

    /**
     * Applies {@code fn} to the tile data.
     *
     * @param fn the function to apply
     * @return a new instance with the tile data updated
     */
    public PlacedTile mapTile(Function<Tile, Tile> fn) {
        return new PlacedTile(fn.apply(tile), position, rotation);
    }

    /**
     * Sets the placement position.
     *
     * @param position the position
     * @return a new instance with the position updated
     */
    public PlacedTile setPosition(Position position) {
        return new PlacedTile(tile, position, rotation);
    }

    /**
     * Sets the placement rotation.
     *
     * @param rotation the rotation
     * @return a new instance with the rotation updated
     */
    public PlacedTile setRotation(Rotation rotation) {
        return new PlacedTile(tile, position, rotation);
    }

    /**
     * Gets the edge pattern of the tile.
     *
     * @return the edge pattern
     */
    public EdgePattern getEdgePattern() {
        return tile.getEdgePattern().rotate(rotation);
    }

    /**
     * Gets the features of the tile at the given location.
     *
     * @param loc the location of interest
     * @return the features of the tile at the given location
     */
    public Tuple2<FeaturePointer, Feature> getInitialFeaturePartOf(Location loc) {
        Location initialLoc = loc.rotateCCW(getRotation());
        return tile
            .getInitialFeatures()
            .find(t -> initialLoc.isPartOf(t._1.getLocation()))
            .getOrNull();
    }

    @Override
    public String toString() {
        return tile + "," + position + "," + rotation;
    }
}
