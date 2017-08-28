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

@Immutable
public class TileDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ABBEY_TILE_ID = "AM.A";

    private final Expansion origin;
    private final String id;
    private final EdgePattern edgePattern;
    private final TileSymmetry symmetry;

    private final Map<Location, Feature> initialFeatures;

    //expansions data - maybe some map instead ? but still it is only few tiles
    private final TileTrigger trigger;
    private final Location flier;
    private final Location windRose;
    private final Class<? extends Feature> cornCircle;

    public TileDefinition(Expansion origin, String id, Map<Location, Feature> initialFeatures) {
        this(origin, id, initialFeatures, null, null, null, null);
    }

    public TileDefinition(Expansion origin, String id,
        Map<Location, Feature> initialFeatures,
        TileTrigger trigger, Location flier, Location windRose,
        Class<? extends Feature> cornCircle) {
        this.origin = origin;
        this.id = id;
        this.initialFeatures = initialFeatures;

        this.trigger = trigger;
        this.flier = flier;
        this.windRose = windRose;
        this.cornCircle = cornCircle;

        this.edgePattern = computeEdgePattern();
        this.symmetry = this.edgePattern.getSymmetry();
    }

    public TileDefinition setTileTrigger(TileTrigger trigger) {
        assert this.trigger == null;
        return new TileDefinition(origin, id, initialFeatures, trigger, flier, windRose, cornCircle);
    }

    public TileDefinition setFlier(Location flier) {
        return new TileDefinition(origin, id, initialFeatures, trigger, flier, windRose, cornCircle);
    }

    public TileDefinition setWindRose(Location windRose) {
        return new TileDefinition(origin, id, initialFeatures, trigger, flier, windRose, cornCircle);
    }

    public TileDefinition setCornCircle(Class<? extends Feature> cornCircle) {
        return new TileDefinition(origin, id, initialFeatures, trigger, flier, windRose, cornCircle);
    }

    public TileDefinition setInitialFeatures(Map<Location, Feature> initialFeatures) {
        return new TileDefinition(origin, id, initialFeatures, trigger, flier, windRose, cornCircle);
    }

    public TileDefinition addBridge(Location bridgeLoc) {
        assert bridgeLoc == Location.NS || bridgeLoc == Location.WE;
        Bridge bridge = new Bridge(bridgeLoc);
        return setInitialFeatures(initialFeatures.put(bridgeLoc, bridge));
    }

    public boolean isAbbeyTile() {
        return id.equals(ABBEY_TILE_ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, initialFeatures);
    }

    public Expansion getOrigin() {
        return origin;
    }

    public String getId() {
        return id;
    }

    public EdgePattern getEdgePattern() {
        return edgePattern;
    }

    public TileSymmetry getSymmetry() {
        return symmetry;
    }

    public Map<Location, Feature> getInitialFeatures() {
        return initialFeatures;
    }

    public TileTrigger getTrigger() {
        return trigger;
    }

    public Location getFlier() {
        return flier;
    }

    public Location getWindRose() {
        return windRose;
    }

    public Class<? extends Feature> getCornCircle() {
        return cornCircle;
    }

    public boolean hasTower() {
        return initialFeatures.containsKey(Location.TOWER);
    }

    private EdgeType computeSideEdge(Location loc) {
        Tuple2<Location, Feature> tuple = initialFeatures.find(item -> loc.isPartOf(item._1)).getOrNull();

        if (tuple == null) return EdgeType.FARM;
        if (tuple._2 instanceof Road) return EdgeType.ROAD;
        if (tuple._2 instanceof City) return EdgeType.CITY;
        if (tuple._2 instanceof River) return EdgeType.RIVER;

        throw new IllegalArgumentException();
    }

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
