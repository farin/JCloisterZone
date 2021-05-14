package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class Farm extends TileFeature implements Scoreable, MultiTileFeature<Farm> {

    // for unplaced features, references is to (0, 0)
    protected final Set<FeaturePointer> adjoiningCities; //or castles
    protected final boolean adjoiningCityOfCarcassonne;
    protected final int pigHerds;


    public Farm(List<FeaturePointer> places, Set <FeaturePointer> adjoiningCities) {
        this(places, adjoiningCities, false, 0);
    }

    public Farm(List<FeaturePointer> places, Set<FeaturePointer> adjoiningCities,
            boolean adjoiningCityOfCarcassonne, int pigHerds) {
        super(places);
        this.adjoiningCities = adjoiningCities;
        this.adjoiningCityOfCarcassonne = adjoiningCityOfCarcassonne;
        this.pigHerds = pigHerds;
    }

    @Override
    public Farm merge(Farm farm) {
        assert farm != this;
        return new Farm(
            mergePlaces(farm),
            mergeAdjoiningCities(farm),
            adjoiningCityOfCarcassonne || farm.adjoiningCityOfCarcassonne,
            pigHerds + farm.pigHerds
        );
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Farm(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardAdjoiningCities(pos, rot),
            adjoiningCityOfCarcassonne,
            pigHerds
        );
    }

    public boolean isOpen(GameState state) {
        List<FeaturePointer> places = getPlaces();
        if (places.length() == 1) {
            Location loc = places.get().getLocation();
            if (loc == Location.INNER_FARM || loc == Location.INNER_FARM_B) {
                return false;
            }
            // otherwise it can be still closed when placed next to Abbey
        }
        for (FeaturePointer fp : places) {
            Position pos = fp.getPosition();
            for (Location loc : fp.getLocation().splitToFarmSides().map(Location::farmToSide).distinct()) {
                if (!state.getPlacedTiles().containsKey(pos.add(loc))) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<FeaturePointer> getAdjoiningCities() {
        return adjoiningCities;
    }

    public Farm setAdjoiningCities(Set<FeaturePointer> adjoiningCities) {
        return new Farm(places, adjoiningCities, adjoiningCityOfCarcassonne, pigHerds);
    }

    public int getPigHerds() {
        return pigHerds;
    }

    public Farm setPigHerds(int pigHerds) {
        return new Farm(places, adjoiningCities, adjoiningCityOfCarcassonne, pigHerds);
    }

    public boolean isAdjoiningCityOfCarcassonne() {
        return adjoiningCityOfCarcassonne;
    }

    public Farm setAdjoiningCityOfCarcassonne(boolean adjoiningCityOfCarcassonne) {
        return new Farm(places, adjoiningCities, adjoiningCityOfCarcassonne, pigHerds);
    }


    private int getPigCount(GameState state, Player player) {
        return getSpecialMeeples(state).count(m -> (m instanceof Pig) && m.getPlayer().equals(player));
    }

    private PointsExpression getPoints(GameState state, Player player, String exprName, int basePoints) {
        Map<String, Integer> args = HashMap.empty();
        int pigCount = getPigCount(state, player);
        if (pigCount > 0) args = args.put("pigs", pigCount);
        if (pigHerds > 0) args = args.put("pigHerds", pigHerds);
        int pointsPerCity = basePoints + pigHerds + pigCount;
        return getCityPoints(state, exprName, pointsPerCity, args).merge(getLittleBuildingPoints(state));
    }

    public PointsExpression getPoints(GameState state, Player player) {
        return getPoints(state, player, "farm", 3);
    }

    public PointsExpression getPointsWhenBarnIsConnected(GameState state, Player player) {
        return getPoints(state, player, "farm.barn-connected", 1);
    }


    public PointsExpression getBarnPoints(GameState state) {
        //no pig herds according to Complete Annotated Rules
        Map<String, Integer> args = HashMap.empty();
        return getCityPoints(state, "farm.barn", 4, args).merge(getLittleBuildingPoints(state));
    }

    private PointsExpression getCityPoints(GameState state, String exprName, int pointsPerCity, Map<String, Integer> args) {
        int points = 0;
        if (adjoiningCityOfCarcassonne) {
            args = args.put("coc", 1);
            points += pointsPerCity;
        }
        int castleCount = 0;
        int cityCount = 0;
        int besiegedCount = 0;

        Set<Feature> features = adjoiningCities.map(fp -> state.getFeature(fp));
        for (Feature feature : features) {
            if (feature instanceof Castle) {
                // adjoining Castle provides 1 more point then common city
                points += pointsPerCity + 1;
                castleCount++;
            } else {
                City city = (City) feature;
                if (city.isCompleted(state)) {
                    points += pointsPerCity;
                    if (city.hasModifier(SiegeCapability.BESIEGED)) {
                        // besieged cities has double value
                        points += pointsPerCity;
                        besiegedCount++;
                    } else {
                        cityCount++;
                    }
                }
            }
        }
        if (cityCount > 0) args = args.put("cities", cityCount);
        if (besiegedCount > 0) args = args.put("besieged", besiegedCount);
        if (castleCount > 0) args = args.put("castles", castleCount);
        return new PointsExpression(points, exprName, args);
    }

    public static String name() {
        return "Farm";
    }

    // immutable helpers

    protected Set<FeaturePointer> mergeAdjoiningCities(Farm obj) {
        return this.adjoiningCities.union(obj.adjoiningCities);
    }

    protected Set<FeaturePointer> placeOnBoardAdjoiningCities(Position pos, Rotation rot) {
        return this.adjoiningCities.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
