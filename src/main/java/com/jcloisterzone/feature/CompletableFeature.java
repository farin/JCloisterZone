package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public abstract class CompletableFeature<T extends CompletableFeature<?>> extends TileFeature implements Completable, MultiTileFeature<T> {

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

    // helpers

    protected int getMageAndWitchPoints(GameState state, int points) {
        Stream<Special> specials = getSpecialMeeples(state);
        if (!specials.find(Predicates.instanceOf(Mage.class)).isEmpty()) {
            points += getPlaces().size();
        }
        if (!specials.find(Predicates.instanceOf(Witch.class)).isEmpty()) {
            if (points % 2 == 1) points++;
            points /= 2;
        }
        return points;
    }

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