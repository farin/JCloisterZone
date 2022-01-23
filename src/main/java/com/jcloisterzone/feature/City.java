package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.ShortEdge;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.modifier.BooleanAnyModifier;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.feature.modifier.IntegerAddModifier;
import com.jcloisterzone.feature.modifier.IntegerNonMergingModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.BardsLuteCapability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.setup.GameElementQuery;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.*;

import java.util.ArrayList;

public class City extends CompletableFeature<City> implements ModifiedFeature<City> {

    private static final long serialVersionUID = 1L;

    public static final IntegerAddModifier PENNANTS = new IntegerAddModifier("city[pennants]", null);
    public static final BooleanAnyModifier DARMSTADTIUM = new BooleanAnyModifier("city[darmstadtium]", null);
    public static final BooleanAnyModifier BESIEGED = new BooleanAnyModifier("city[besieged]", new GameElementQuery("siege"));
    public static final BooleanAnyModifier CATHEDRAL = new BooleanAnyModifier("city[cathedral]", new GameElementQuery("cathedral"));
    public static final BooleanAnyModifier PRINCESS = new BooleanAnyModifier("city[princess]", new GameElementQuery("princess"));
    public static final IntegerNonMergingModifier POINTS_MODIFIER = new IntegerNonMergingModifier("city[points]", null);

    private final Set<Tuple2<ShortEdge, FeaturePointer>> multiEdges; // HS.CC!.v abstraction, multiple cities can connect to same edge
    private final Map<FeatureModifier<?>, Object> modifiers;

    public City(List<FeaturePointer> places, Set<Edge> openEdges, Map<FeatureModifier<?>, Object> modifiers) {
        this(places, openEdges, HashSet.empty(), HashSet.empty(), modifiers);
    }

    public City(List<FeaturePointer> places,
            Set<Edge> openEdges, Set<FeaturePointer> neighboring,
            Set<Tuple2<ShortEdge, FeaturePointer>> multiEdges,
            Map<FeatureModifier<?>, Object> modifiers) {
        super(places, openEdges, neighboring);
        this.multiEdges = multiEdges;
        this.modifiers = modifiers;
    }

    @Override
    public Map<FeatureModifier<?>, Object> getModifiers() {
        return modifiers;
    }

    @Override
    public City setModifiers(Map<FeatureModifier<?>, Object> modifiers) {
        if (this.modifiers == modifiers) return this;
        return new City(places, openEdges, neighboring, multiEdges, modifiers);
    }

    @Override
    public City merge(City city) {
        assert city != this;
        return new City(
            mergePlaces(city),
            mergeEdges(city),
            mergeNeighboring(city),
            mergeMultiEdges(city),
            mergeModifiers(city)
        );
    }

    @Override
    public City closeEdge(Edge edge) {
        return new City(
            places,
            openEdges.remove(edge),
            neighboring,
            multiEdges.filter(me -> !me._1.equals(edge)),
            modifiers
        );
    }

    @Override
    public City setOpenEdges(Set<Edge> openEdges) {
        return new City(places, openEdges, neighboring, multiEdges, modifiers);
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new City(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardEdges(pos, rot),
            placeOnBoardNeighboring(pos, rot),
            placeOnBoardMultiEdges(pos, rot),
            modifiers
        );
    }

    public City setMultiEdges(Set<Tuple2<ShortEdge, FeaturePointer>> multiEdges) {
        if (this.multiEdges == multiEdges) return this;
        return new City(places, openEdges, neighboring, multiEdges, modifiers);
    }

    public Set<Tuple2<ShortEdge, FeaturePointer>> getMultiEdges() {
		return multiEdges;
	}

    @Override
    public City setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new City(places, openEdges, neighboring, multiEdges, modifiers);
    }

    @Override
    public PointsExpression getStructurePoints(GameState state, boolean completed) {
        Integer points = getModifier(state, POINTS_MODIFIER, null);
        boolean tinyCity = false;
        var exprItems = new ArrayList<ExprItem>();

        if (completed) {
            int bardsNotesCount = 0;
            BardsLuteCapability bardsLute = state.getCapabilities().get(BardsLuteCapability.class);
            if (bardsLute != null) {
                bardsNotesCount = bardsLute.getPlacedTokensCount(state, places);
                if (bardsNotesCount>0) {
                    exprItems.add(new ExprItem("bardsnotes", tileCount * bardsNotesCount));
                }
	        }
        }

        if (points != null) {
            exprItems.add(new ExprItem(1, "tiles", points));
        } else {
            int tileCount = getTilePositions().size();
            int pennants = getModifier(state, PENNANTS, 0);
            boolean cathedral = hasModifier(state, CATHEDRAL);

            if (cathedral && !completed) {
                return new PointsExpression("city.incomplete", new ExprItem("cathedral", 0));
            }

            tinyCity = completed && tileCount == 2 && "2".equals(state.getStringRule(Rule.TINY_CITY_SCORING));
            boolean besieged = hasModifier(state, BESIEGED);

            exprItems.add(new ExprItem(tileCount, "tiles", tileCount * (completed && !tinyCity ? 2 : 1)));
            if (pennants > 0) {
                exprItems.add(new ExprItem(pennants, "pennants", completed && !tinyCity ? 2 * pennants : pennants));
            }
            if (besieged) {
                exprItems.add(new ExprItem("besieged", -tileCount - pennants));
            }
            if (cathedral) {
                exprItems.add(new ExprItem("cathedral", tileCount + pennants));
            }
            if (completed && hasModifier(state, DARMSTADTIUM)) {
                exprItems.add(new ExprItem("darmstadtium", 3));
            }

            scoreScriptedModifiers(state, exprItems, java.util.Map.of("tiles", tileCount, "completed", completed));
        }

        String name = "city";
        if (tinyCity) {
            name = "city.tiny";
        } else {
            if (!completed) name = "city.incomplete";
        }
        return new PointsExpression(name, List.ofAll(exprItems));
    }

    @Override
    public PointsExpression getPoints(GameState state) {
        PointsExpression basePoints = getStructurePoints(state, isCompleted(state));
        return getMageAndWitchPoints(state, basePoints).appendAll(getLittleBuildingPoints(state));
    }


    public static String name() {
        return "City";
    }

    protected Set<Tuple2<ShortEdge, FeaturePointer>> mergeMultiEdges(City city) {
    	return multiEdges.addAll(city.multiEdges);
    }

    protected Set<Tuple2<ShortEdge, FeaturePointer>> placeOnBoardMultiEdges(Position pos, Rotation rot) {
    	return multiEdges.map(t -> {
    		ShortEdge edge = t._1.rotateCW(Position.ZERO, rot).translate(pos);
    		FeaturePointer fp = t._2.rotateCW(rot).translate(pos);
    		return new Tuple2<>(edge, fp);
    	});
    }
}
