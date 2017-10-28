package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._tr;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeGoods;
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

public class City extends CompletableFeature<City> {

    private static final long serialVersionUID = 1L;

    private final int pennants;
    private final Map<TradeGoods, Integer> tradeGoods;
    private final boolean besieged, cathedral, princess, castleBase;

    public City(List<FeaturePointer> places, Set<Edge> openEdges, int pennants) {
        this(places, openEdges, HashSet.empty(), pennants, HashMap.empty(), false, false, false, false);
    }

    public City(List<FeaturePointer> places,
            Set<Edge> openEdges, Set<FeaturePointer> neighboring,
            int pennants,
            Map<TradeGoods, Integer> tradeGoods, boolean besieged, boolean cathedral, boolean princess,
            boolean castleBase) {
        super(places, openEdges, neighboring);
        this.pennants = pennants;
        this.tradeGoods = tradeGoods;
        this.besieged = besieged;
        this.cathedral = cathedral;
        this.princess = princess;
        this.castleBase = castleBase;
    }

    @Override
    public City merge(City city) {
        assert city != this;
        return new City(
            mergePlaces(city),
            mergeEdges(city),
            mergeNeighboring(city),
            pennants + city.pennants,
            mergeTradeGoods(city),
            besieged || city.besieged,
            cathedral || city.cathedral,
            princess || city.princess,
            castleBase && city.castleBase
        );
    }

    @Override
    public City mergeAbbeyEdge(Edge edge) {
        return new City(
            places,
            openEdges.remove(edge),
            neighboring,
            pennants,
            tradeGoods,
            besieged ,
            cathedral,
            princess,
            castleBase
        );
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new City(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardEdges(pos, rot),
            placeOnBoardNeighboring(pos, rot),
            pennants, tradeGoods, besieged, cathedral, princess, castleBase
        );
    }

    protected Map<TradeGoods, Integer> mergeTradeGoods(City city) {
        return tradeGoods.merge(city.tradeGoods, (a, b) -> a + b);
    }

    public City setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new City(places, openEdges, neighboring, pennants, tradeGoods, besieged, cathedral, princess, castleBase);
    }

    public boolean isBesieged() {
        return besieged;
    }

    public City setBesieged(boolean besieged) {
        if (this.besieged == besieged) return this;
        return new City(places, openEdges, neighboring, pennants, tradeGoods, besieged, cathedral, princess, castleBase);
    }

    public boolean isCathedral() {
        return cathedral;
    }

    public City setCathedral(boolean cathedral) {
        if (this.cathedral == cathedral) return this;
        return new City(places, openEdges, neighboring, pennants, tradeGoods, besieged, cathedral, princess, castleBase);
    }

    public boolean isPrincess() {
        return princess;
    }

    public City setPrincess(boolean princess) {
        if (this.princess == princess) return this;
        return new City(places, openEdges, neighboring, pennants, tradeGoods, besieged, cathedral, princess, castleBase);
    }

    public boolean isCastleBase() {
        return castleBase;
    }

    public City setCastleBase(boolean castleBase) {
        if (this.castleBase == castleBase) return this;
        return new City(places, openEdges, neighboring, pennants, tradeGoods, besieged, cathedral, princess, castleBase);
    }

    public int getPennants() {
        return pennants;
    }

    public Map<TradeGoods, Integer> getTradeGoods() {
        return tradeGoods;
    }

    public City setTradeGoods(Map<TradeGoods, Integer> tradeGoods) {
        return new City(places, openEdges, neighboring, pennants, tradeGoods, besieged, cathedral, princess, castleBase);
    }

    @Override
    public int getPoints(GameState state) {
        boolean completed = isCompleted(state);
        int tileCount = getTilePositions().size();

        int pointsPerUnit = 2;
        if (besieged) pointsPerUnit--;
        if (completed) {
            if (cathedral) pointsPerUnit++;
        } else {
            if (cathedral) {
                pointsPerUnit = 0;
            } else {
                pointsPerUnit--;
            }
        }
        return getMageAndWitchPoints(state, pointsPerUnit * (tileCount + pennants)) + getLittleBuildingPoints(state);
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CITY;
    }

    public static String name() {
        return _tr("City");
    }
}
