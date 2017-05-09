package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class Road extends CompletableFeature<Road> {

    private static final long serialVersionUID = 1L;

    private final boolean inn;
    private final Map<FeaturePointer, TunnelEnd> tunnelEnds;

    public Road(List<FeaturePointer> places, Set<Edge> openEdges) {
        this(places, openEdges, HashSet.empty(), false, HashMap.empty());
    }

    public Road(
            List<FeaturePointer> places,
            Set<Edge> openEdges,
            Set<FeaturePointer> neighboring,
            boolean inn,
            Map<FeaturePointer, TunnelEnd> tunnelEnds
        ) {
        super(places, openEdges, neighboring);
        this.inn = inn;
        this.tunnelEnds = tunnelEnds;
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
            tunnelEnds
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
        return new Road(places, openEdges, neighboring, inn, tunnelEnds);
    }

    public Map<FeaturePointer, TunnelEnd> getTunnelEnds() {
        return tunnelEnds;
    }

    public Road setTunnelEnds(Map<FeaturePointer, TunnelEnd> tunnelEnds) {
        if (this.tunnelEnds == tunnelEnds) return this;
        return new Road(places, openEdges, neighboring, inn, tunnelEnds);
    }

    public Road setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Road(places, openEdges, neighboring, inn, tunnelEnds);
    }

//
//    public boolean isTunnelEnd() {
//        return tunnelEnd != 0;
//    }
//
//    public boolean isTunnelOpen() {
//        return tunnelEnd == OPEN_TUNNEL;
//    }
//
//    public void setTunnelEdge(MultiTileFeature f) {
//        edges[edges.length - 1] = f;
//    }

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
        return _("Road");
    }

    // immutable helpers

    protected Map<FeaturePointer, TunnelEnd> mergeTunnelEnds(Road road) {
        return tunnelEnds.merge(road.tunnelEnds);
    }

    protected Map<FeaturePointer, TunnelEnd> placeOnBoardTunnelEnds(Position pos, Rotation rot) {
        return tunnelEnds.mapKeys(fp -> fp.rotateCW(rot).translate(pos));
    }
}
