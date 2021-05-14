package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.ShortEdge;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.feature.modifier.IntegerModifier;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.CathedralCapability;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.capability.TradeGoodsCapability.TradeGoods;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.*;

public class City extends CompletableFeature<City> implements ModifiedFeature<City> {

    private static final long serialVersionUID = 1L;

    public static IntegerModifier PENNANTS = new IntegerModifier("pennants");
    public static IntegerModifier EXTRA_POINTS = new IntegerModifier("extra-points");

    private final Set<Tuple2<ShortEdge, FeaturePointer>> multiEdges; // HS.CC!.v abstraction, multiple cities can connect to same edge
    private final Map<FeatureModifier<?>, Object> modifiers;

    public City(List<FeaturePointer> places, Set<Edge> openEdges, Map<FeatureModifier<?>, Object> modifiers) {
        this(places, openEdges, HashSet.empty(), HashSet.empty(), HashMap.empty());
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
    public City mergeAbbeyEdge(Edge edge) {
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
        boolean tinyCity = false;
        int tileCount = getTilePositions().size();

        int pennants = getModifier(PENNANTS, 0);
        int extraPoints = getModifier(EXTRA_POINTS, 0);

        Map<String, Integer> args = HashMap.of(
                "tiles", tileCount,
                "pennants", pennants,
                "extraPoints", extraPoints
        );

        boolean cathedral = hasModifier(CathedralCapability.CATHEDRAL);

        if (cathedral && !completed) {
            return new PointsExpression(0, "city.incomplete-cathedral", args);
        }

        int pointsPerUnit = 2;
        if (completed && tileCount == 2 && "2".equals(state.getStringRule(Rule.TINY_CITY_SCORING))) {
            tinyCity = true;
            pointsPerUnit = 1;
        } else{
            boolean besieged = hasModifier(SiegeCapability.BESIEGED);
            if (besieged) {
                args = args.put("besieged", 1);
                pointsPerUnit--;
            }
        }

        if (cathedral) {
            pointsPerUnit++;
            args = args.put("cathedral", 1);
        }

        if (!completed) {
            pointsPerUnit--;
        }

        int points = pointsPerUnit * (tileCount + pennants) + extraPoints;
        String name = "city";
        if (tinyCity) {
            name = "city.tiny";
        } else {
            if (!completed) name = "city.incomplete";
        }
        return new PointsExpression(points, name, args);
    }

    @Override
    public PointsExpression getPoints(GameState state) {
        PointsExpression basePoints = getStructurePoints(state, isCompleted(state));
        return getMageAndWitchPoints(state, basePoints).merge(getLittleBuildingPoints(state));
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
