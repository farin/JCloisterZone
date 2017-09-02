package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;
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

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.FARM;
    }

    private int getPointsPerCity(GameState state, Player player, int basePoints) {
        return basePoints + pigHerds
            + getSpecialMeeples(state).count(m -> (m instanceof Pig) && m.getPlayer().equals(player));
    }

    public int getPoints(GameState state, Player player) {
        return getCityPoints(state, getPointsPerCity(state, player, 3)) + getLittleBuildingPoints(state);
    }

    public int getPointsWhenBarnIsConnected(GameState state, Player player) {
        return getCityPoints(state, getPointsPerCity(state, player, 1)) + getLittleBuildingPoints(state);
    }

    public int getBarnPoints(GameState state) {
        //no pig herds according to Complete Annotated Rules
        return getCityPoints(state, 4) + getLittleBuildingPoints(state);
    }

    private int getCityPoints(GameState state, int pointsPerCity) {
        int points = adjoiningCityOfCarcassonne ? pointsPerCity : 0;
        Set<Feature> features = adjoiningCities.map(fp -> state.getFeature(fp));

        for (Feature feature : features) {
            if (feature instanceof Castle) {
                // adjoining Castle provides 1 more point then common city
                points += pointsPerCity + 1;
            } else {
                City city = (City) feature;
                if (city.isCompleted(state)) {
                    points += pointsPerCity;
                    if (city.isBesieged()) {
                        // besieged cities has double value
                        points += pointsPerCity;
                    }
                }
            }
        }
        return points;
    }

    public static String name() {
        return _("Farm");
    }

    // immutable helpers

    protected Set<FeaturePointer> mergeAdjoiningCities(Farm obj) {
        return this.adjoiningCities.union(obj.adjoiningCities);
    }

    protected Set<FeaturePointer> placeOnBoardAdjoiningCities(Position pos, Rotation rot) {
        return this.adjoiningCities.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
