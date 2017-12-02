package com.jcloisterzone.ai.player;

import com.jcloisterzone.Player;
import com.jcloisterzone.ai.GameStateRanking;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.CloisterLike;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.CompletableFeature;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.ScoreCompletable;
import com.jcloisterzone.reducers.ScoreFarm;
import com.jcloisterzone.reducers.ScoreFarmBarn;

import io.vavr.Predicates;
import io.vavr.Tuple2;
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
    private GameState state;
    private int numberOfPlayers;
    private int remainingTurns;
    private PlacedTile lastPlaced;
    private Map<Position, Double> positionProbability;

    public LegacyRanking(Player me) {
        super();
        this.me = me;
    }

    @Override
    public Double apply(GameState state) {
        double ranking = 0.0;

        this.state = state;
        numberOfPlayers = state.getPlayers().length();
        lastPlaced = state.getLastPlaced();
        positionProbability = getPositionProbability();
        remainingTurns = (int) Math.ceil(state.getTilePack().totalSize() / numberOfPlayers);

        ranking += ratePoints();
        ranking += rateUnfinishedFeatures();
        ranking += rateMeeples();
        ranking += rateBoardShape();
        ranking += rateDragon();

        return ranking;
    }

    private Map<Position, Double> getPositionProbability() {

        Map<EdgePattern, Integer> packPatterns = state.getTilePack().getPatterns();
        return state.getAvailablePlacements().toMap(avail -> {
            int matchingTiles = 0;
            for (Tuple2<EdgePattern, Integer> pattern : packPatterns) {
                if (avail._2.isMatchingAnyRotation(pattern._1)) {
                    matchingTiles += pattern._2;
                    continue;
                }
            }
            double prob = 0.0;
            if (matchingTiles > 0) {
                prob = 1.0 - Math.pow(1.0 - (1.0 / numberOfPlayers), matchingTiles);
            }
            return new Tuple2<Position, Double>(avail._1, prob);
        });
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

        for (Completable origCompletable : getOccupiedScoreables(state, Completable.class)) {
            Completable completable = origCompletable;
            // reset inn/cathedral to get non zero points
            if (completable instanceof City) {
                completable = ((City) completable).setCathedral(false);
            }
            if (completable instanceof Road) {
                completable = ((Road) completable).setInn(false);
            }

            ScoreFeatureReducer sr = new ScoreCompletable(completable, true);
            sr.apply(state); //no assign!

            double prob = 1.0;
            Stream<Position> adjacent = null;

            if (completable instanceof CloisterLike) {
                CloisterLike cl = (CloisterLike) completable;
                Position pos = cl.getTilePositions().get();
                adjacent = Stream.ofAll(Position.ADJACENT_AND_DIAGONAL.values())
                    .map(offset -> pos.add(offset))
                    .filter(p -> positionProbability.containsKey(p));


            } else if (completable instanceof CompletableFeature) {
                CompletableFeature<?> cf = (CompletableFeature<?>) completable;
                adjacent = Stream.ofAll(cf.getOpenEdges().map(edge -> {
                    if (positionProbability.containsKey(edge.getP1())) {
                        return edge.getP1();
                    } else {
                        return edge.getP2();
                    }
                }));
            }

            if (adjacent != null) {
                prob = adjacent
                  .map(p -> positionProbability.get(p).getOrNull())
                  .min()
                  .getOrNull();
            }

            Tuple2<Double, Double> penalty = null;
            if (prob < 0.0001) {
                penalty = new Tuple2<>(12.0, 3.0);
            } else if (prob < 0.2) {
                penalty = new Tuple2<>(3.0, 0.75);
            } else if (prob < 0.55) {
                penalty = new Tuple2<>(1.2, 0.3);
            }

            //TODO compare with number of available followers instead
            //count them once and use here
            if (remainingTurns > 7) {
                if (penalty != null) {
                    for (Follower follower : completable.getFollowers(state)) {
                        if (follower.getPlayer() == me) {
                            r -= penalty._1;
                        } else {
                            r += penalty._2;
                        }
                    }
                }
            }

            for (Player player : sr.getOwners()) {
                double q = 1.0;
                if (completable instanceof City) {
                    if (((City)origCompletable).isCathedral()) {
                        q = 2.8 * prob;
                    } else {
                        q = 1.0 + prob * 0.8;
                    }
                }
                if (completable instanceof Road) {
                    if (((Road)origCompletable).isInn()) {
                        q = 1.8 * prob;
                    }
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

    private double rateDragon() {
        Position pos = state.getNeutralFigures().getDragonDeployment();
        if (pos == null) {
            return 0.;
        }
        double r = 0.0;
        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            int dist = t._2.getPosition().squareDistance(pos);
            if (dist > 3) continue;
            r += (t._1.getPlayer() == me ? -1.0 : 1.0) * (3 - dist);
        }
        return r;
    }

    private double rateBoardShape() {
        return 0.0001 * state.getAdjacentTiles2(lastPlaced.getPosition()).size();
    }

}