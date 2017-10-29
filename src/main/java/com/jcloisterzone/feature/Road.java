package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._tr;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class Road extends CompletableFeature<Road> {

    private static final long serialVersionUID = 1L;

    private final boolean inn;
    private final Set<FeaturePointer> openTunnelEnds;

    public Road(List<FeaturePointer> places, Set<Edge> openEdges) {
        this(places, openEdges, HashSet.empty(), false, HashSet.empty());
    }

    public Road(
            List<FeaturePointer> places,
            Set<Edge> openEdges,
            Set<FeaturePointer> neighboring,
            boolean inn,
            Set<FeaturePointer> openTunnelEnds
        ) {
        super(places, openEdges, neighboring);
        this.inn = inn;
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
            placeOnBoardTunnelEnds(pos, rot)
        );
    }

    public boolean isInn() {
        return inn;
    }

    public Road setInn(boolean inn) {
        if (this.inn == inn) return this;
        return new Road(places, openEdges, neighboring, inn, openTunnelEnds);
    }

    public Set<FeaturePointer> getOpenTunnelEnds() {
        return openTunnelEnds;
    }

    public Road setOpenTunnelEnds(Set<FeaturePointer> openTunnelEnds) {
        if (this.openTunnelEnds == openTunnelEnds) return this;
        return new Road(places, openEdges, neighboring, inn, openTunnelEnds);
    }

    public Road setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Road(places, openEdges, neighboring, inn, openTunnelEnds);
    }

    @Override
    public int getPoints(GameState state) {
        int tileCount = getTilePositions().size();
        int points;
        if (inn) {
            points = isCompleted(state) ? tileCount * 2 : 0;
        } else {
            points = tileCount;
        }
        return getMageAndWitchPoints(state, points) + getLittleBuildingPoints(state);
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.ROAD;
    }

    public static String name() {
        return _tr("Road");
    }

    // immutable helpers

    protected Set<FeaturePointer> mergeTunnelEnds(Road road) {
        return openTunnelEnds.union(road.openTunnelEnds);
    }

    protected Set<FeaturePointer> placeOnBoardTunnelEnds(Position pos, Rotation rot) {
        return openTunnelEnds.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
