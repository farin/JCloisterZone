package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.modifier.BooleanAnyModifier;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.BardsLuteCapability;
import com.jcloisterzone.game.capability.FerriesCapability;
import com.jcloisterzone.game.capability.FerriesCapabilityModel;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.setup.GameElementQuery;
import com.jcloisterzone.game.setup.RuleQuery;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTunnelToken;
import io.vavr.Tuple2;
import io.vavr.collection.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.function.Function;

public class Road extends CompletableFeature<Road> implements ModifiedFeature<Road> {

    private static final long serialVersionUID = 1L;

    public static final BooleanAnyModifier INN = new BooleanAnyModifier("road[inn]", new GameElementQuery("inn"));
    public static final BooleanAnyModifier LABYRINTH = new BooleanAnyModifier("road[labyrinth]", new RuleQuery(Rule.LABYRINTH_VARIANT, "advanced"));
    public static final BooleanAnyModifier ROBBERS_SON = new BooleanAnyModifier("road[robbers-son]", new GameElementQuery("robbers-son"));

    private final Map<FeatureModifier<?>, Object> modifiers;
    private final Set<FeaturePointer> openTunnelEnds;

    public Road(List<FeaturePointer> places, Set<Edge> openEdges, Map<FeatureModifier<?>, Object> modifiers) {
        this(places, openEdges, HashSet.empty(), modifiers, HashSet.empty());
    }

    public Road(
            List<FeaturePointer> places,
            Set<Edge> openEdges,
            Set<FeaturePointer> neighboring,
            Map<FeatureModifier<?>, Object> modifiers,
            Set<FeaturePointer> openTunnelEnds
        ) {
        super(places, openEdges, neighboring);
        this.modifiers = modifiers;
        this.openTunnelEnds = openTunnelEnds;
    }

    @Override
    public boolean isOpen(GameState state) {
        return super.isOpen(state) || !openTunnelEnds.isEmpty();
    }

    public boolean isLabyrinth(GameState state) {
        return this.hasModifier(state, LABYRINTH);
    }

    @Override
    public Map<FeatureModifier<?>, Object> getModifiers() {
        return modifiers;
    }

    @Override
    public Road setModifiers(Map<FeatureModifier<?>, Object> modifiers) {
        if (this.modifiers == modifiers) return this;
        return new Road(places, openEdges, neighboring, modifiers, openTunnelEnds);
    }

    @Override
    public boolean isMergeableWith(EdgeFeature<?> other) {
        return other instanceof Road; // this handles Bridges
    }

    @Override
    public Road merge(Road road) {
        assert road != this;
        return new Road(
            mergePlaces(road),
            mergeEdges(road),
            mergeNeighboring(road),
            mergeModifiers(road),
            mergeTunnelEnds(road)
        );
    }

    @Override
    public Road closeEdge(Edge edge) {
        return new Road(
            places,
            openEdges.remove(edge),
            neighboring,
            modifiers,
            openTunnelEnds
        );
    }

    @Override
    public Road setOpenEdges(Set<Edge> openEdges) {
        return new Road(
            places,
            openEdges,
            neighboring,
            modifiers,
            openTunnelEnds
        );
    }

    /** Merge roads through connecting tunnel ends. */
    public Road connectTunnels(Road road, FeaturePointer tunnelEnd1, FeaturePointer tunnelEnd2) {
        Road merged;
        if (this == road) {
            // just remove openTunnelEnds
            merged = this;
        } else {
            merged = merge(road);
        }
        return merged.setOpenTunnelEnds(
            merged.openTunnelEnds.remove(tunnelEnd1).remove(tunnelEnd2)
        );
    }

    @Override
    public Road placeOnBoard(Position pos, Rotation rot) {
        return new Road(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardEdges(pos, rot),
            placeOnBoardNeighboring(pos, rot),
            modifiers,
            placeOnBoardTunnelEnds(pos, rot)
        );
    }

    public Set<FeaturePointer> getOpenTunnelEnds() {
        return openTunnelEnds;
    }

    public Road setOpenTunnelEnds(Set<FeaturePointer> openTunnelEnds) {
        if (this.openTunnelEnds == openTunnelEnds) return this;
        return new Road(places, openEdges, neighboring, modifiers, openTunnelEnds);
    }

    @Override
    public Road setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Road(places, openEdges, neighboring, modifiers, openTunnelEnds);
    }

    @Override
    public PointsExpression getStructurePoints(GameState state, boolean completed) {
        int tileCount = getTilePositions().size();
        Map<String, Integer> args = HashMap.of("tiles", tileCount);

        boolean inn = hasModifier(state, INN);
        boolean labyrinth = hasModifier(state, LABYRINTH);

        if (inn && !completed) {
            return new PointsExpression("road.incomplete", new ExprItem("inn", 0));
        }

        var exprItems = new ArrayList<ExprItem>();
        exprItems.add(new ExprItem(tileCount, "tiles", tileCount));

        if (inn) {
            exprItems.add(new ExprItem("inn", tileCount));
        }
        if (labyrinth && completed) {
            int meeplesCount = getMeeples(state).size();
            exprItems.add(new ExprItem(meeplesCount, "meeples", 2 * meeplesCount));
        }

        if (completed) {
            int bardsNotesCount = 0;
            BardsLuteCapability bardsLute = state.getCapabilities().get(BardsLuteCapability.class);
            if (bardsLute != null) {
                bardsNotesCount = bardsLute.getPlacedTokensCount(state, places);
                if (bardsNotesCount>0) {
                    exprItems.add(new ExprItem(bardsNotesCount, "bardsnotes", tileCount * bardsNotesCount));
                }
	        }
        }

        scoreScriptedModifiers(state, exprItems, java.util.Map.of("tiles", tileCount, "completed", completed));
        return new PointsExpression(completed ? "road" : "road.incomplete", List.ofAll(exprItems));
    }

    @Override
    public PointsExpression getPoints(GameState state) {
        PointsExpression basePoints = getStructurePoints(state, isCompleted(state));
        return getMageAndWitchPoints(state, basePoints).appendAll(getLittleBuildingPoints(state));
    }

    public static String name() {
        return "Road";
    }

    private FeaturePointer findPartOf(Iterable<FeaturePointer> list, FeaturePointer fp) {
        for (FeaturePointer item : list) {
            if (fp.isPartOf(item)) {
               return item;
            }
        }
        return null;
    }

    private void iterateParts(GameState state, FeaturePointer from, Function<FeaturePointer, Boolean> callback) {
        java.util.Set<FeaturePointer> places = this.places.toJavaSet();
        places.remove(from);
        Deque<FeaturePointer> queue = new ArrayDeque<>();
        queue.push(from);

        Map<FeaturePointer, PlacedTunnelToken> placedTunnels = state.getCapabilityModel(TunnelCapability.class);
        FerriesCapabilityModel ferriesModel = state.getCapabilityModel(FerriesCapability.class);

        while (!queue.isEmpty()) {
            FeaturePointer fp = queue.pop();

            if (!callback.apply(fp)) {
                continue;
            }

            for (FeaturePointer adj : fp.getAdjacent()) {
                FeaturePointer place = findPartOf(places, adj);
                if (place != null && places.remove(place)) {
                    queue.push(place);
                }
            }

            if (placedTunnels != null) {
                PlacedTunnelToken placedTunnel = placedTunnels.get(fp).getOrNull();
                if (placedTunnel != null) {
                    FeaturePointer place = placedTunnels
                            .find(t ->
                                    t._2 != null && t._2 != placedTunnel
                                            && t._2.getToken() == placedTunnel.getToken()
                                            && t._2.getPlayerIndex() == placedTunnel.getPlayerIndex()
                            )
                            .map(Tuple2::_1)
                            .getOrNull();
                    if (place != null && places.remove(place)) {
                        queue.push(place);
                    }
                }
            }

            if (ferriesModel != null) {
                FeaturePointer ferry = ferriesModel.getFerries().find(fp::isPartOf).getOrNull();
                if (ferry != null) {
                    FeaturePointer place = ferry.setLocation(ferry.getLocation().subtract(fp.getLocation()));

                    if (place != null && places.remove(place)) {
                        queue.push(place);
                    }
                }
            }
        }
    }

    /**
     * Follow road up to nearest parts matching given predicate
     */
    public List<FeaturePointer> findNearest(GameState state, FeaturePointer from, Function<FeaturePointer, Boolean> predicate) {
        java.util.Set<FeaturePointer> result = new java.util.HashSet<>();

        iterateParts(state, from, fp -> {
            boolean match = predicate.apply(fp);
            if (match) {
                result.add(fp);
                return false;
            }
            return true;
        });
        return List.ofAll(result);
    }

    public List<FeaturePointer> findSegmentBorderedBy(GameState state, FeaturePointer from, Function<FeaturePointer, Boolean> predicate) {
        java.util.Set<FeaturePointer> result = new java.util.HashSet<>();
        iterateParts(state, from, fp -> {
            boolean match = predicate.apply(fp);
            if (match) {
                return false;
            }
            result.add(fp);
            return true;
        });
        return List.ofAll(result);
    }

    // immutable helpers

    protected Set<FeaturePointer> mergeTunnelEnds(Road road) {
        return openTunnelEnds.union(road.openTunnelEnds);
    }

    protected Set<FeaturePointer> placeOnBoardTunnelEnds(Position pos, Rotation rot) {
        return openTunnelEnds.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
