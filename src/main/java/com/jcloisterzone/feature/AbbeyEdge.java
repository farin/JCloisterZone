package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;

public class AbbeyEdge extends TileFeature implements EdgeFeature<AbbeyEdge> {

    public AbbeyEdge(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public boolean isMergeableWith(EdgeFeature<?> other) {
        return false;
    }

    @Override
    public AbbeyEdge closeEdge(Edge edge) {
        return this;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new AbbeyEdge(placeOnBoardPlaces(pos, rot));
    }

    @Override
    public FeaturePointer getProxyTarget() {
        return new FeaturePointer(places.get().getPosition(), Monastery.class, Location.I);
    }
}