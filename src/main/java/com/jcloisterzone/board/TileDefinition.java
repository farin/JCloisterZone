package com.jcloisterzone.board;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.River;
import com.jcloisterzone.feature.Road;

import io.vavr.Tuple2;
import io.vavr.collection.Map;

/**
 * The type Tile definition.
 */
// TODO rename to Tile
@Immutable
public class TileDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The constant ABBEY_TILE_ID.
     */
    public static final String ABBEY_TILE_ID = "AM.A";

    private final Expansion origin;
    private final String id;
    private final EdgePattern edgePattern;
    private final TileSymmetry symmetry;

    private final Map<Location, Feature> initialFeatures;

    private final TileTrigger trigger;
    private final Location windRose;
    private final Class<? extends Feature> cornCircle;

    /**
     * Instantiates a new {@code TileDefinition}
     *
     * @param origin          the {@link Expansion} this tile belongs to
     * @param id              the identifier of the tile
     * @param initialFeatures the {@link Feature}s of the tile
     */
    public TileDefinition(Expansion origin, String id, Map<Location, Feature> initialFeatures) {
        this(origin, id, initialFeatures, null, null, null);
    }

    /**
     * Instantiates a new {@code TileDefinition}.
     *
     * @param origin          the {@link Expansion} this tile belongs to
     * @param id              the identifier of the tile
     * @param initialFeatures the {@link Feature}s of the tile
     * @param trigger         the tile trigger (a tag for some special behaviour)
     * @param flier           the direction pointed by of the flier ({@see FlierCapability})
     * @param windRose        the direction pointed by the wind rose ({@see WindRoseCapability})
     * @param cornCircle      the feature on the corn circle, if any ({@see CornCircleCapability})
     */
    public TileDefinition(Expansion origin, String id,
        Map<Location, Feature> initialFeatures,
        TileTrigger trigger, Location windRose,
        Class<? extends Feature> cornCircle) {
        this.origin = origin;
        this.id = id;
        this.initialFeatures = initialFeatures;

        this.trigger = trigger;
        this.windRose = windRose;
        this.cornCircle = cornCircle;

        this.edgePattern = computeEdgePattern();
        this.symmetry = this.edgePattern.getSymmetry();
    }

    /**
     * Sets the tile trigger
     *
     * @param trigger the trigger to set
     * @return a new instance with the trigger set
     */
    public TileDefinition setTileTrigger(TileTrigger trigger) {
        assert this.trigger == null;
        return new TileDefinition(origin, id, initialFeatures, trigger, windRose, cornCircle);
    }

    /**
     * Sets the direction pointed by the wind rose.
     * {@see WindRoseCapability}
     *
     * @param windRose the direction to set
     * @return a new instance with the direction pointed by the wind rose set
     */
    public TileDefinition setWindRose(Location windRose) {
        return new TileDefinition(origin, id, initialFeatures, trigger, windRose, cornCircle);
    }

    /**
     * Sets the feature on the corn circle (if any).
     * {@see CornCircleCapability}
     *
     * @param cornCircle the feature to set
     * @return a new instance with the corn circle feature set
     */
    public TileDefinition setCornCircle(Class<? extends Feature> cornCircle) {
        return new TileDefinition(origin, id, initialFeatures, trigger, windRose, cornCircle);
    }

    /**
     * Sets the tile features
     *
     * @param initialFeatures the features to set
     * @return a new instance with the features set
     */
    public TileDefinition setInitialFeatures(Map<Location, Feature> initialFeatures) {
        return new TileDefinition(origin, id, initialFeatures, trigger, windRose, cornCircle);
    }

    /**
     * Adds a bridge to the instance
     *
     * @param bridgeLoc the location where the bridge spans
     * @return a new instance with the bridge added
     */
    public TileDefinition addBridge(Location bridgeLoc) {
        assert bridgeLoc == Location.NS || bridgeLoc == Location.WE;
        Bridge bridge = new Bridge(bridgeLoc);
        return setInitialFeatures(initialFeatures.put(bridgeLoc, bridge));
    }

    /**
     * Checks if {@code this} is an abbey tile.
     * {@see AbbeyCapability}
     *
     * @return {@code true} if {@code this} is an abbey tile., {@code false} otherwise
     */
    public boolean isAbbeyTile() {
        return id.equals(ABBEY_TILE_ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, initialFeatures);
    }

    /**
     * Gets the {@link Expansion} this tile belongs to.
     *
     * @return the {@link Expansion} this tile belongs to
     */
    public Expansion getOrigin() {
        return origin;
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
     * Gets the trigger of this tile.
     *
     * @return the trigger of this tile
     */
    public TileTrigger getTrigger() {
        return trigger;
    }

    /**
     * Gets the direction pointed by the wind rose.
     *
     * @return the direction pointed by the wind rose
     */
    public Location getWindRose() {
        return windRose;
    }

    /**
     * Gets the feature on the corn circle (if any).
     * {@see CornCircleCapability}
     *
     * @return the feature on the corn circle, if any
     */
    public Class<? extends Feature> getCornCircle() {
        return cornCircle;
    }

    /**
     * Checks whether this tile has a tower place on it
     *
     * @return {@code true} it this tile has a tower place on it, {@code false} otherwise
     */
    public boolean hasTower() {
        return initialFeatures.containsKey(Location.TOWER);
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
