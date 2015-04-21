package com.jcloisterzone.feature.visitor.score;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.SiegeCapability;

public class FarmScoreContext extends MultiTileScoreContext {

    private Map<City, CityScoreContext> adjoiningCompletedCities = new HashMap<>();
    private Set<Castle> adjoiningCastles = new HashSet<>();
    private boolean adjoiningCityOfCarcassonne;
    private Set<Player> pigs = new HashSet<>();
    private boolean pigHerd;

    private Map<City, CityScoreContext> cityCache;

    public FarmScoreContext(Game game) {
        super(game);
    }

    public Map<City, CityScoreContext> getCityCache() {
        return cityCache;
    }

    public void setCityCache(Map<City, CityScoreContext> cityCache) {
        this.cityCache = cityCache;
    }

    private void addAdjoiningCompletedCities(Feature[] adjoiningCities) {
        for (Feature feature : adjoiningCities) {
            if (feature instanceof City) {
                City c = (City) feature;
                CityScoreContext ctx = cityCache.get(c);
                if (ctx == null) {
                    ctx = c.getScoreContext();
                    ctx.setCityCache(cityCache);
                    c.walk(ctx);
                }
                if (ctx.isCompleted()) {
                    adjoiningCompletedCities.put((City) ctx.getMasterFeature(), ctx);
                }
            } else if (feature instanceof Castle) {
                adjoiningCastles.add((Castle) feature.getMaster());
            }
        }
    }

    @Override
    public boolean visit(Feature feature) {
        Farm farm = (Farm) feature;
        if (farm.isAdjoiningCityOfCarcassonne()) {
            adjoiningCityOfCarcassonne = true;
        }
        if (farm.getAdjoiningCities() != null) {
            addAdjoiningCompletedCities(farm.getAdjoiningCities());
        }
        for (Meeple m : farm.getMeeples()) {
            if (m instanceof Pig) {
                pigs.add(m.getPlayer());
            }
        }
        if (farm.isPigHerd()) {
            pigHerd = true;
        }
        return super.visit(feature);
    }

    private int getPointsPerCity(Player player, int basePoints) {
        int pointsPerCity = basePoints + (pigHerd ? 1 : 0);
        if (pigs.contains(player)) pointsPerCity += 1;
        return pointsPerCity;
    }

    public int getPoints(Player player) {
        return getPlayerPoints(player, getPointsPerCity(player, 3)) + getLittleBuildingPoints();
    }

    public int getPointsWhenBarnIsConnected(Player player) {
        return getPlayerPoints(player, getPointsPerCity(player, 1)) + getLittleBuildingPoints();
    }

    private int getPlayerPoints(Player player, int pointsPerCity) {

        int points = adjoiningCityOfCarcassonne ? pointsPerCity : 0;
        points += (pointsPerCity + 1) * adjoiningCastles.size();

        //optimalization
        if (!getGame().hasCapability(SiegeCapability.class)) {
            points += pointsPerCity * adjoiningCompletedCities.size();
            return points;
        }

        for (CityScoreContext ctx : adjoiningCompletedCities.values()) {
            points += pointsPerCity;
            if (ctx.isBesieged()) { //count city twice
                points += pointsPerCity;
            }
        }
        return points;
    }

    public int getBarnPoints() {
        //note: pigHerd has no influence on barn points
        int points = adjoiningCityOfCarcassonne ? 4 : 0;
        points += 5 * adjoiningCastles.size();
        if (getGame().hasCapability(SiegeCapability.class)) {
            for (CityScoreContext ctx : adjoiningCompletedCities.values()) {
                points += 4;
                if (ctx.isBesieged()) { //count city twice
                    points += 4;
                }
            }
        } else {
            points += adjoiningCompletedCities.size() * 4;
        }
        return points;
    }

}
