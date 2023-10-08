package com.jcloisterzone.feature;

import java.util.ArrayList;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class River extends CompletableFeature<River> implements ModifiedFeature<River> {

    private final Map<FeatureModifier<?>, Object> modifiers;

    public River(List<FeaturePointer> places, Set<Edge> openEdges, Map<FeatureModifier<?>, Object> modifiers) {
        this(places, openEdges, HashSet.empty(),modifiers);
    }

    public River(List<FeaturePointer> places, 
    		Set<Edge> openEdges, 
    		Set<FeaturePointer> neighboring,
    		Map<FeatureModifier<?>, Object> modifiers) {
        super(places, openEdges, neighboring);
    	this.modifiers = modifiers;
    }

    @Override
    public Map<FeatureModifier<?>, Object> getModifiers() {
        return modifiers;
    }

    @Override
    public River setModifiers(Map<FeatureModifier<?>, Object> modifiers) {
        if (this.modifiers == modifiers) return this;
        return new River(places, openEdges, neighboring, modifiers);
    }

    @Override
    public River setOpenEdges(Set<Edge> openEdges) {
        return new River(places, openEdges, neighboring, modifiers);
    }
    @Override
    public River placeOnBoard(Position pos, Rotation rot) {
        return new River(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardEdges(pos, rot),
            placeOnBoardNeighboring(pos, rot),
            modifiers
        );
    }

    @Override
    public River setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new River(places, openEdges, neighboring, modifiers);
    }

    @Override
    public River merge(River river) {
        assert river != this;
        return new River(
            mergePlaces(river),
            mergeEdges(river),
            mergeNeighboring(river),
            mergeModifiers(river)
        );
    }

    @Override
    public River closeEdge(Edge edge) {
        return new River(
            places,
            openEdges.remove(edge),
            neighboring,
            modifiers
        );
    }

    @Override
    public PointsExpression getStructurePoints(GameState state, boolean completed) {
        int tileCount = getTilePositions().size();

        var exprItems = new ArrayList<ExprItem>();
        exprItems.add(new ExprItem(tileCount, "tiles", (completed ? 2 : 1) * tileCount ));

        scoreScriptedModifiers(state, exprItems, java.util.Map.of("tiles", tileCount, "completed", completed));
        return new PointsExpression(completed ? "river" : "river.incomplete", List.ofAll(exprItems));
    }

    @Override
    public PointsExpression getPoints(GameState state) {
        PointsExpression basePoints = getStructurePoints(state, isCompleted(state));
        return getMageAndWitchPoints(state, basePoints).appendAll(getLittleBuildingPoints(state));
    }

    public static String name() {
        return "River";
    }
}
