package com.jcloisterzone.ai.player;

import com.jcloisterzone.Player;
import com.jcloisterzone.ai.GameStateRanking;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.ScoreCompletable;
import com.jcloisterzone.reducers.ScoreFarm;
import com.jcloisterzone.reducers.ScoreFarmBarn;

import io.vavr.Predicates;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;

class LegacyRanking implements GameStateRanking {

    public static final double[]  OPEN_ROAD_PENALTY =
        { 0.0, 1.0, 2.5, 4.5, 7.5, 10.5, 14.5, 19.0, 29.0 };
    public static final double[]  OPEN_CITY_PENALTY =
        { 0.0, 0.5, 1.5, 3.0, 5.0, 8.0, 12.0, 17.0, 27.0 };
    public static final double[]  OPEN_FARM_PENALTY =
        { 0.0, 5.0, 10.0, 19.0, 28.0, 37.0, 47.0, 57.0, 67.0 };
    public static final double[]  OPEN_CLOISTER_PENALTY =
        { 0.0, 0.0, 0.4, 0.8, 1.2, 2.0, 4.0, 7.0, 11.0 };

    private final Player me;

    // ranking context
    private PlacedTile lastPlaced;
    private GameState state;

    public LegacyRanking(Player me) {
        super();
        this.me = me;
    }

    @Override
    public Double apply(GameState state) {
        double ranking = 0.0;

        this.state = state;
        lastPlaced = state.getLastPlaced();

        //state.getAva
        //state.getTilePack().

        ranking += ratePoints();
        ranking += rateUnfinishedFeatures();
        ranking += rateMeeples();
        ranking += rateBoardShape();

        return ranking;
    }

    private double ratePoints() {
        double r = 0.0;
        PlayersState ps = state.getPlayers();
        for (Player player : ps.getPlayers()) {
            double q = player == me ? 1.0 : -1.0;
            r += q * ps.getScore().get(player.getIndex()).getPoints();
        }
        return r;
    }

    private <T extends Scoreable> Stream<T> getOccupiedScoreables(GameState state, Class<T> cls) {
        return state.getFeatures(cls).filter(c -> c.isOccupied(state));
    }

    private double rateUnfinishedFeatures() {
        double r = 0.0;

        for (Completable completable : getOccupiedScoreables(state, Completable.class)) {
            ScoreFeatureReducer sr = new ScoreCompletable(completable, true);
            sr.apply(state); //no assign!
            for (Player player : sr.getOwners()) {
                double q = 1.0;
                if (completable instanceof City) {
                    q = 1.8;
                }
                if (player != me) {
                    q = -q;
                }
                r += q * sr.getFeaturePoints(player);
            }
        }

        //USE AS SCORING RESULT
        for (Farm farm : getOccupiedScoreables(state, Farm.class)) {
            boolean hasBarn = farm.getSpecialMeeples(state)
                .find(Predicates.instanceOf(Barn.class)).isDefined();
            ScoreFeatureReducer sr;
            if (hasBarn) {
               sr = new ScoreFarmBarn(farm, true);
            } else {
               sr = new ScoreFarm(farm, true);
            }
            sr.apply(state);
            for (Player player : sr.getOwners()) {
                double q = 0.99;
                if (player != me) {
                    q = -q;
                }
                r += q * sr.getFeaturePoints(player);
            }
        }

        Array<Seq<Follower>> followers = state.getPlayers().getFollowers();

        @SuppressWarnings("rawtypes")
        Map<Class, Integer> placedOn = followers
            .get(me.getIndex())
            .groupBy(f -> {
                Feature feature = f.getFeature(state);
                return (Class) (feature == null ? null : feature.getClass());
            })
            .mapValues(seq -> seq.size());

        int open = placedOn.get(Road.class).getOrElse(0);
        r -= OPEN_ROAD_PENALTY[open];
        open = placedOn.get(City.class).getOrElse(0);
        r -= OPEN_CITY_PENALTY[open];
        open = placedOn.get(Cloister.class).getOrElse(0);
        r -= OPEN_CLOISTER_PENALTY[open];
        open = placedOn.get(Farm.class).getOrElse(0);
        r -= OPEN_FARM_PENALTY[open];

        for (Player player : state.getPlayers().getPlayers()) {
            if (player == me) continue;
            // penalty for followers in supply
            r -= followers.get(player.getIndex())
                .filter(f -> f.isInSupply(state))
                .size();
        }

        return r;
    }

    private double rateMeeples() {
        double r = 0.0;
        for (Player player : state.getPlayers().getPlayers()) {
            double q = player == me ? 1.0 : -1.0;
            for (Follower f : player.getFollowers(state).filter(f -> f.isInSupply(state))) {
                //instanceof cannot be used because of Phantom
                if (f.getClass().equals(SmallFollower.class)) {
                    r += q * 0.15;
                } else {
                    r += q * 0.25;
                }
            }
        }
        return r;
    }

    private double rateBoardShape() {
        return 0.0001 * state.getAdjacentTiles2(lastPlaced.getPosition()).size();
    }

}