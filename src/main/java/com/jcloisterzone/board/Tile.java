package com.jcloisterzone.board;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.feature.*;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a tile type
 */
@Immutable
public class Tile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final EdgePattern edgePattern;
    private final TileSymmetry symmetry;

    private final Map<Location, Feature> initialFeatures;
    private final Set<TileModifier> modifiers;

    /**
     * Instantiates a new {@code TileDefinition}
     *
     * @param id              the identifier of the tile
     * @param initialFeatures the {@link Feature}s of the tile
     */
    public Tile(String id, Map<Location, Feature> initialFeatures) {
        this(id, initialFeatures, HashSet.empty());
    }

    /**
     * Instantiates a new {@code TileDefinition}.
     *
     * @param id              the identifier of the tile
     * @param initialFeatures the {@link Feature}s of the tile
     * @param modifiers
     */
    public Tile(String id, Map<Location, Feature> initialFeatures, Set<TileModifier> modifiers) {
        this.id = id;
        this.initialFeatures = initialFeatures;
        this.modifiers = modifiers;

        this.edgePattern = computeEdgePattern();
        this.symmetry = this.edgePattern.getSymmetry();
    }


    public Tile addTileModifier(TileModifier modifier) {
        return new Tile(id, initialFeatures, modifiers.add(modifier));
    }

    /**
     * Sets the tile features
     *
     * @param initialFeatures the features to set
     * @return a new instance with the features set
     */
    public Tile setInitialFeatures(Map<Location, Feature> initialFeatures) {
        return new Tile(id, initialFeatures, modifiers);
    }

    /**
     * Adds a bridge to the instance
     *
     * @param bridgeLoc the location where the bridge spans
     * @return a new instance with the bridge added
     */
    public Tile addBridge(Location bridgeLoc) {
        assert bridgeLoc == Location.NS || bridgeLoc == Location.WE;
        Bridge bridge = new Bridge(bridgeLoc);
        return setInitialFeatures(initialFeatures.put(bridgeLoc, bridge));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, initialFeatures);
    }


    /**
     * Gets the id of this tile.
     *
     * @return the id of this tile
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the edge pattern of this tile.
     *
     * @return the edge pattern of this tile
     */
    public EdgePattern getEdgePattern() {
        return edgePattern;
    }

    /**
     * Gets the symmetry of this tile.
     *
     * @return the symmetry of this tile
     */
    public TileSymmetry getSymmetry() {
        return symmetry;
    }

    /**
     * Gets the features of this tile.
     *
     * @return the features of this tile
     */
    public Map<Location, Feature> getInitialFeatures() {
        return initialFeatures;
    }

    /**
     * Gets all tile modifiers of this tile.
     *
     * @return the trigger of this tile
     */
    public Set<TileModifier> getTileModifiers() {
        return modifiers;
    }

    /**
     * Checks whether this tile has a modifier
     */
    public boolean hasModifier(TileModifier modifier) {
    	return modifiers.contains(modifier);
    }

    /**
     * Calculates and returns the type of the edge pointed by {@code loc}.
     *
     * @param loc the location indicating the edge of interest
     * @return the edge type
     */
    private EdgeType computeSideEdge(Location loc) {
        Tuple2<Location, Feature> tuple = initialFeatures.find(item -> loc.isPartOf(item._1)).getOrNull();

        if (tuple == null) return EdgeType.FARM;
        if (tuple._2 instanceof Road) return EdgeType.ROAD;
        if (tuple._2 instanceof City) return EdgeType.CITY;
        if (tuple._2 instanceof River) return EdgeType.RIVER;

        throw new IllegalArgumentException();
    }

    /**
     * Calculates and returns the edge pattern of this tile.
     *
     * @return the edge pattern of this tile.
     */
    private EdgePattern computeEdgePattern() {
        return new EdgePattern(
            computeSideEdge(Location.N),
            computeSideEdge(Location.E),
            computeSideEdge(Location.S),
            computeSideEdge(Location.W)
        );
    }

    @Override
    public String toString() {
        return id;
    }
}
