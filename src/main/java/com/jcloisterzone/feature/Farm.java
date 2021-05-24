package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.feature.modifier.IntegerAddModifier;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.setup.GameElementQuery;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.*;

import java.util.ArrayList;

public class Farm extends TileFeature implements Scoreable, MultiTileFeature<Farm>, ModifiedFeature<Farm> {

    // TODO rename attribute to pig-herd
    public static final IntegerAddModifier PIG_HERD = new IntegerAddModifier("farm[pig]", new GameElementQuery("pig-herd"));

    // for unplaced features, references is to (0, 0)
    protected final Set<FeaturePointer> adjoiningCities; //or castles
    protected final boolean adjoiningCityOfCarcassonne;

    private final Map<FeatureModifier<?>, Object> modifiers;

    public Farm(List<FeaturePointer> places, Set <FeaturePointer> adjoiningCities) {
        this(places, adjoiningCities, false, HashMap.empty());
    }

    public Farm(List<FeaturePointer> places, Set<FeaturePointer> adjoiningCities,
            boolean adjoiningCityOfCarcassonne, Map<FeatureModifier<?>, Object> modifiers) {
        super(places);
        this.adjoiningCities = adjoiningCities;
        this.adjoiningCityOfCarcassonne = adjoiningCityOfCarcassonne;
        this.modifiers = modifiers;
    }

    @Override
    public Map<FeatureModifier<?>, Object> getModifiers() {
        return modifiers;
    }

    @Override
    public Farm setModifiers(Map<FeatureModifier<?>, Object> modifiers) {
        if (this.modifiers == modifiers) return this;
        return new Farm(places, adjoiningCities, adjoiningCityOfCarcassonne, modifiers);
    }

    @Override
    public Farm merge(Farm farm) {
        assert farm != this;
        return new Farm(
            mergePlaces(farm),
            mergeAdjoiningCities(farm),
            adjoiningCityOfCarcassonne || farm.adjoiningCityOfCarcassonne,
            mergeModifiers(farm)
        );
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Farm(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardAdjoiningCities(pos, rot),
            adjoiningCityOfCarcassonne,
            modifiers
        );
    }

    public boolean isOpen(GameState state) {
        List<FeaturePointer> places = getPlaces();
        if (places.length() == 1) {
            Location loc = places.get().getLocation();
            if (loc.isInner()) {
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
        return new Farm(places, adjoiningCities, adjoiningCityOfCarcassonne, modifiers);
    }

    public boolean isAdjoiningCityOfCarcassonne() {
        return adjoiningCityOfCarcassonne;
    }

    public Farm setAdjoiningCityOfCarcassonne(boolean adjoiningCityOfCarcassonne) {
        return new Farm(places, adjoiningCities, adjoiningCityOfCarcassonne, modifiers);
    }

    private int getPigCount(GameState state, Player player) {
        return getSpecialMeeples(state).count(m -> (m instanceof Pig) && m.getPlayer().equals(player));
    }

    public PointsExpression getPoints(GameState state, String exprSubtitle, Player player) {
        return getCityPoints(state, exprSubtitle == null ? "farm" : "farm." + exprSubtitle, 3, player).appendAll(getLittleBuildingPoints(state));
    }
    public PointsExpression getPointsWhenBarnIsConnected(GameState state, Player player) {
        return getCityPoints(state, "farm.barn-connected", 1, player).appendAll(getLittleBuildingPoints(state));
    }

    public PointsExpression getBarnPoints(GameState state) {
        //no pig herds according to Complete Annotated Rules
        return getCityPoints(state, "farm", 4, null).appendAll(getLittleBuildingPoints(state));
    }

    private PointsExpression getCityPoints(GameState state, String exprName, int pointsPerCity, Player scorePigsForPlayer) {
        int castleCount = 0;
        int cityCount = 0;
        int besiegedCount = 0;

        Set<Feature> features = adjoiningCities.map(fp -> state.getFeature(fp));
        for (Feature feature : features) {
            if (feature instanceof Castle) {
                castleCount++;
            } else {
                City city = (City) feature;
                if (city.isCompleted(state)) {
                    cityCount++;
                    if (city.hasModifier(state, City.BESIEGED)) {
                        besiegedCount++;
                    }
                }
            }
        }

        var exprItems = new ArrayList<ExprItem>();
        if (cityCount > 0) {
            exprItems.add(new ExprItem(cityCount, "cities", cityCount * pointsPerCity));
        }
        if (besiegedCount > 0) {
            // besieged cities has double value, append bonus for them
            exprItems.add(new ExprItem(besiegedCount, "besieged", besiegedCount * pointsPerCity));
        }
        if (castleCount > 0) {
            // adjoining Castle provides 1 more point then common city
            exprItems.add(new ExprItem(castleCount, "castles", castleCount * (pointsPerCity + 1)));
        }
        if (adjoiningCityOfCarcassonne) {
            exprItems.add(new ExprItem("coc", pointsPerCity));
        }

        // besieged is already part of cityCount
        var scoredObjects = cityCount + castleCount + (adjoiningCityOfCarcassonne ? 1 : 0);
        if ( scorePigsForPlayer != null && scoredObjects > 0) {
            int pigCount = getPigCount(state, scorePigsForPlayer);
            int pigHerds = getModifier(state, PIG_HERD, 0);
            if (pigCount > 0) {
                exprItems.add(pigCount, new ExprItem(pigCount, "pigs", pigCount * scoredObjects));
            }
            if (pigHerds > 0) {
                exprItems.add(pigCount, new ExprItem(pigHerds, "pigHerds", pigHerds * scoredObjects));
            }
        }

        return new PointsExpression(exprName, List.ofAll(exprItems));
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
