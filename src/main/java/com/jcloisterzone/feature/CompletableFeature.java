package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public abstract class CompletableFeature<T extends CompletableFeature<?>> extends TileFeature implements Completable, MultiTileFeature<T> {

    private static final long serialVersionUID = 1L;

    protected final Set<Edge> openEdges;
    protected final Set<FeaturePointer> neighboring; //for wagon move

    public CompletableFeature(List<FeaturePointer> places, Set<Edge> openEdges, Set<FeaturePointer> neighboring) {
        super(places);
        this.openEdges = openEdges;
        this.neighboring = neighboring;
    }

    public abstract T mergeAbbeyEdge(Edge edge);
    public abstract T setOpenEdges(Set<Edge> openEdges);
    @Override
    public abstract T setNeighboring(Set<FeaturePointer> neighboring);

    @Override
    public boolean isOpen(GameState state) {
        return !openEdges.isEmpty();
    }

    public Set<Edge> getOpenEdges() {
        return openEdges;
    }

    @Override
    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }

    // helpers

    protected PointsExpression getMageAndWitchPoints(GameState state, PointsExpression expr) {
        Mage mage = state.getNeutralFigures().getMage();
        Witch witch = state.getNeutralFigures().getWitch();
        if (mage != null && mage.getFeature(state) == this) {
            int tileCount = getTilePositions().size();
            expr = expr.append(new ExprItem("mage", tileCount));
        }
        if (witch != null && witch.getFeature(state) == this) {
            int points = expr.getPoints();
            expr = expr.append(new ExprItem("witch", -points / 2));
        }
        return expr;
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