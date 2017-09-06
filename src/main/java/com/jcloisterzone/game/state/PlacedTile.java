package com.jcloisterzone.game.state;

import java.io.Serializable;
import java.util.function.Function;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.feature.Feature;

/**
 * Represents a tile that is placed on the map. This is made of a {@link TileDefinition}, a {@link Position} and an
 * {@link Rotation}.
 */
@Immutable
public class PlacedTile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TileDefinition tile;
    private final Position position;
    private final Rotation rotation;

    /**
     * Instantiates a new {@code PlacedTile}.
     *
     * @param tile     the tile
     * @param position the position
     * @param rotation the rotation
     */
    public PlacedTile(TileDefinition tile, Position position, Rotation rotation) {
        this.tile = tile;
        this.position = position;
        this.rotation = rotation;
    }

    /**
     * Gets the tile data.
     *
     * @return the tile data
     */
    public TileDefinition getTile() {
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
    public PlacedTile setTile(TileDefinition tile) {
        return new PlacedTile(tile, position, rotation);
    }

    /**
     * Applies {@code fn} to the tile data.
     *
     * @param fn the function to apply
     * @return a new instance with the tile data updated
     */
    public PlacedTile mapTile(Function<TileDefinition, TileDefinition> fn) {
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
    public Feature getInitialFeaturePartOf(Location loc) {
        Location initialLoc = loc.rotateCCW(getRotation());
        return tile
            .getInitialFeatures()
            .find(t -> initialLoc.isPartOf(t._1))
            .map(t -> t._2)
            .getOrNull();
    }

    @Override
    public String toString() {
        return tile + "," + position + "," + rotation;
    }
}
