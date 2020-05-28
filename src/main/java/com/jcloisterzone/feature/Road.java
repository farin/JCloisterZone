package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.capability.FerriesCapability;
import com.jcloisterzone.game.capability.FerriesCapabilityModel;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTunnelToken;

import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class Road extends CompletableFeature<Road> {

    private static final long serialVersionUID = 1L;

    private final boolean inn;
    private final boolean labyrinth;
    private final Set<FeaturePointer> openTunnelEnds;

    public Road(List<FeaturePointer> places, Set<Edge> openEdges) {
        this(places, openEdges, HashSet.empty(), false, false, HashSet.empty());
    }

    public Road(
            List<FeaturePointer> places,
            Set<Edge> openEdges,
            Set<FeaturePointer> neighboring,
            boolean inn,
            boolean labyrinth,
            Set<FeaturePointer> openTunnelEnds
        ) {
        super(places, openEdges, neighboring);
        this.inn = inn;
        this.labyrinth = labyrinth;
        this.openTunnelEnds = openTunnelEnds;
    }

    @Override
    public boolean isOpen(GameState state) {
        return super.isOpen(state) || !openTunnelEnds.isEmpty();
    }

    @Override
    public Road merge(Road road) {
        assert road != this;
        return new Road(
            mergePlaces(road),
            mergeEdges(road),
            mergeNeighboring(road),
            inn || road.inn,
            labyrinth || road.labyrinth,
            mergeTunnelEnds(road)
        );
    }

    @Override
    public Road mergeAbbeyEdge(Edge edge) {
        return new Road(
            places,
            openEdges.remove(edge),
            neighboring,
            inn,
            labyrinth,
            openTunnelEnds
        );
    }

    @Override
    public Road setOpenEdges(Set<Edge> openEdges) {
        return new Road(
            places,
            openEdges,
            neighboring,
            inn,
            labyrinth,
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
            inn,
            labyrinth,
            placeOnBoardTunnelEnds(pos, rot)
        );
    }

    public boolean isInn() {
        return inn;
    }

    public Road setInn(boolean inn) {
        if (this.inn == inn) return this;
        return new Road(places, openEdges, neighboring, inn, labyrinth, openTunnelEnds);
    }

    public boolean isLabyrinth() {
        return labyrinth;
    }

    public Road setLabyrinth(boolean labyrinth) {
        if (this.labyrinth == labyrinth) return this;
        return new Road(places, openEdges, neighboring, inn, labyrinth, openTunnelEnds);
    }

    public Set<FeaturePointer> getOpenTunnelEnds() {
        return openTunnelEnds;
    }

    public Road setOpenTunnelEnds(Set<FeaturePointer> openTunnelEnds) {
        if (this.openTunnelEnds == openTunnelEnds) return this;
        return new Road(places, openEdges, neighboring, inn, labyrinth, openTunnelEnds);
    }

    @Override
    public Road setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Road(places, openEdges, neighboring, inn, labyrinth, openTunnelEnds);
    }

    private int getBasePoints(GameState state, boolean completed) {
        int tileCount = getTilePositions().size();
        if (inn && !completed) {
            return 0;
        }
        int points = inn ? tileCount * 2 : tileCount;
        if (labyrinth && completed) {
            points += 2 * getMeeples(state).size();
        }
        return points;

    }

    @Override
    public int getPoints(GameState state) {
        int basePoints = getBasePoints(state, isCompleted(state));
        return getMageAndWitchPoints(state, basePoints) + getLittleBuildingPoints(state);
    }

    @Override
    public int getStructurePoints(GameState state, boolean completed) {
        return getBasePoints(state, completed) + getLittleBuildingPoints(state);
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.ROAD;
    }

    public static String name() {
        return _tr("Road");
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
        Deque<FeaturePointer> queue = new ArrayDeque<FeaturePointer>();
        queue.push(from);

        Map<FeaturePointer, PlacedTunnelToken> placedTunnels = state.getCapabilityModel(TunnelCapability.class);
        FerriesCapabilityModel ferriesModel = state.getCapabilityModel(FerriesCapability.class);

        while (!queue.isEmpty()) {
            FeaturePointer fp = queue.pop();

            if (!callback.apply(fp)) {
                continue;
            }

            for (FeaturePointer adj : fp.getAdjacent(Road.class)) {
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
                FeaturePointer ferry = ferriesModel.getFerries().find(f -> fp.isPartOf(f)).getOrNull();
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
