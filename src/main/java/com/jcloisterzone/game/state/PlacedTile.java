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

@Immutable
public class PlacedTile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TileDefinition tile;
    private final Position position;
    private final Rotation rotation;

    public PlacedTile(TileDefinition tile, Position position, Rotation rotation) {
        this.tile = tile;
        this.position = position;
        this.rotation = rotation;
    }

    public TileDefinition getTile() {
        return tile;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public PlacedTile setTile(TileDefinition tile) {
        return new PlacedTile(tile, position, rotation);
    }

    public PlacedTile mapTile(Function<TileDefinition, TileDefinition> fn) {
        return new PlacedTile(fn.apply(tile), position, rotation);
    }

    public PlacedTile setPosition(Position position) {
        return new PlacedTile(tile, position, rotation);
    }

    public PlacedTile setRotation(Rotation rotation) {
        return new PlacedTile(tile, position, rotation);
    }

    public EdgePattern getEdgePattern() {
        return tile.getEdgePattern().rotate(rotation);
    }

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
