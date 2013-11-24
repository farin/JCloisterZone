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

public class FarmScoreContext extends AbstractScoreContext {

    private Map<City, CityScoreContext> adjoiningCompletedCities = new HashMap<>();
    private Set<Castle> adjoiningCastles = new HashSet<>();
    private Set<Player> pigs = new HashSet<>();
    private int pigHerds = 0;

    private Map<City, CityScoreContext> cityCache;
    private Map<Player, Set<City>> scoredCities;

    public FarmScoreContext(Game game) {
        super(game);
    }

    public Map<City, CityScoreContext> getCityCache() {
        return cityCache;
    }

    public void setCityCache(Map<City, CityScoreContext> cityCache) {
        this.cityCache = cityCache;
    }

    public Map<Player, Set<City>> getScoredCities() {
        return scoredCities;
    }

    public void setScoredCities(Map<Player, Set<City>> scoredCities) {
        this.scoredCities = scoredCities;
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
        if (farm.getAdjoiningCities() != null) {
            addAdjoiningCompletedCities(farm.getAdjoiningCities());
        }
        for (Meeple m : farm.getMeeples()) {
            if (m instanceof Pig) {
                pigs.add(m.getPlayer());
            }
        }
        if (farm.isPigHerd()) {
            pigHerds += 1;
        }
        return super.visit(feature);
    }

    private int getPointsPerCity(Player player, int basePoints) {
        int pointsPerCity = basePoints + pigHerds;
        if (pigs.contains(player)) pointsPerCity += 1;
        return pointsPerCity;
    }

    public int getPointsPerCity(Player player) {
        return getPointsPerCity(player, 3);
    }

    public int getPoints(Player player) {
        return getPlayerPoints(player, getPointsPerCity(player));
    }

    public int getPointsWhenBarnIsConnected(Player player) {
        return getPlayerPoints(player, getPointsPerCity(player, 1));
    }

    private int getPlayerPoints(Player player, int pointsPerCity) {
        //optimalization
        if (scoredCities == null && !getGame().hasCapability(SiegeCapability.class)) {
            return pointsPerCity * adjoiningCompletedCities.size() +
                   (pointsPerCity + 1) * adjoiningCastles.size();
        }

        int points = 0;
        for (CityScoreContext ctx : adjoiningCompletedCities.values()) {
            if (scoredCities != null) {
                if (scoredCities.get(player).contains(ctx.getMasterFeature())) {
                    continue;
                }
                scoredCities.get(player).add((City) ctx.getMasterFeature());
            }
            points += pointsPerCity;
            if (ctx.isBesieged()) { //count city twice
                points += pointsPerCity;
            }
        }
        points += (pointsPerCity + 1) * adjoiningCastles.size();
        return points;
    }

    public int getBarnPoints() {
        if (getGame().hasCapability(SiegeCapability.class)) {
            int points = 0;
            for (CityScoreContext ctx : adjoiningCompletedCities.values()) {
                points += 4;
                if (ctx.isBesieged()) { //count city twice
                    points += 4;
                }
            }
            points += 5 * adjoiningCastles.size();
            return points;
        } else {
            return adjoiningCompletedCities.size() * 4 +
                   adjoiningCastles.size() * 5;
        }
    }

}
