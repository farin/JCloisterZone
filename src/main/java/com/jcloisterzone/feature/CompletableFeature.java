package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;
import io.vavr.collection.Set;

public abstract class CompletableFeature<T extends CompletableFeature<?>> extends ScoreableFeature implements Completable, MultiTileFeature<T> {

    private static final long serialVersionUID = 1L;

    protected final Set<Edge> openEdges; //TODO change to Set
    protected final Set<FeaturePointer> neighboring; //for wagon move

    public CompletableFeature(List<FeaturePointer> places, Set<Edge> openEdges, Set<FeaturePointer> neighboring) {
        super(places);
        this.openEdges = openEdges;
        this.neighboring = neighboring;
    }

    public abstract T mergeAbbeyEdge(Edge edge);
    public abstract T setNeighboring(Set<FeaturePointer> neighboring);

    @Override
    public boolean isOpen(GameState state) {
        return !openEdges.isEmpty();
    }

    public Set<Edge> getOpenEdges() {
        return openEdges;
    }

    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }

    public Set<Position> getTilePositions() {
        return getPlaces().map(fp -> fp.getPosition()).toSet();
    }

    // immutable helpers

    protected Set<Edge> mergeEdges(T obj) {
        Set<Edge> connectedEdges = openEdges.intersect(obj.openEdges);
        return openEdges.union(obj.openEdges).diff(connectedEdges);
    }

    protected Set<FeaturePointer> mergeNeighboring(T obj) {
        return neighboring.addAll(obj.neighboring);
    }

    protected Set<Edge> placeOnBoardEdges(Position pos, Rotation rot) {
        return openEdges.map(edge -> edge.rotateCW(Position.ZERO, rot).translate(pos));
    }

    protected Set<FeaturePointer> placeOnBoardNeighboring(Position pos, Rotation rot) {
        return neighboring.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}