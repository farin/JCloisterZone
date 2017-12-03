package com.jcloisterzone.ai.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.ai.GameStateRanking;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.CloisterLike;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.CompletableFeature;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.ScoreFarm;
import com.jcloisterzone.reducers.ScoreFarmBarn;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

class LegacyRanking implements GameStateRanking {

    protected final transient Logger logger = LoggerFactory.getLogger("AI Ranking");

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
    private java.util.HashMap<Edge, CompletableRanking> edges;

    public LegacyRanking(Player me) {
        super();
        this.me = me;
    }

    @Override
    public Double apply(GameState state) {
        double ranking = 0.0;
        double r;

        this.state = state;
        numberOfPlayers = state.getPlayers().length();
        lastPlaced = state.getLastPlaced();
        positionProbability = getPositionProbability();
        remainingTurns = (int) Math.ceil(state.getTilePack().totalSize() / numberOfPlayers);
        edges = new java.util.HashMap<>();

        logger.debug("--> {}", lastPlaced);
        r = ratePoints();
        ranking += r;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("  > Points              %8.5f", r));
        }
        //logger.debug("  > Unfinished features");
        r = rateUnfinishedFeatures();
        ranking += r;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("  > Unfinished features       %8.5f", r));
        }
        r = rateOpenFeatures();
        ranking += r;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("  > Open features       %8.5f", r));
        }
        r = rateMeeples();
        ranking += r;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("  > Meeples             %8.5f", r));
        }
        r = rateBoardShape();
        ranking += r;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("  > Board shape         %8.5f", r));
        }
        r = rateDragon();
        ranking += r;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("  > Dragon              %8.5f", r));
        }
        r = rateConnections();
        ranking += r;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("  > Connections         %8.5f", r));
            logger.debug(String.format("--> Total               %8.5f", ranking));
        }
        return ranking;
    }

    private double ptsforPlayer(Player p, double pts) {
        return p == me ? pts : -pts;
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
            r += ptsforPlayer(player, ps.getScore().get(player.getIndex()).getPoints());
        }
        return r;
    }

    private double countCompleteProbability(Completable completable) {
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
              .foldLeft(1.0, (res, p) -> res * p);
        }

        return prob;
    }

    private double getCompletablePoints(CompletableRanking cr) {
        double prob = cr.getProbability();
        // apply threshold
        if (prob > 0.85) {
            prob = 1.0;
        }
        if (cr.getFeature() instanceof CloisterLike) {
            int uncertain = 9 - cr.getIncompletePoints();
            return cr.getIncompletePoints() + prob * 0.5 * uncertain;
        } else {
            int uncertain = cr.getCompletePoints() - cr.getIncompletePoints();
            // multiply with 0.8 to advantage closed features
            return cr.getIncompletePoints() + prob * 0.8 * uncertain;
        }
    }

    private double rateOpenFeatures() {
        double r = 0.0;
        Array<Seq<Follower>> followers = state.getPlayers().getFollowers();
        for (Player player : state.getPlayers().getPlayers()) {
            @SuppressWarnings("rawtypes")
            Map<Class, Integer> placedOn = followers
                .get(player.getIndex())
                .groupBy(f -> {
                    Feature feature = f.getFeature(state);
                    if (feature == null) return null;
                    if (feature instanceof CloisterLike) return (Class) Cloister.class;
                    return feature.getClass();
                })
                .mapValues(seq -> seq.size());

            double pr = 0.0;
            int open = placedOn.get(Road.class).getOrElse(0);
            pr -= OPEN_ROAD_PENALTY[open];
            open = placedOn.get(City.class).getOrElse(0);
            pr -= OPEN_CITY_PENALTY[open];
            open = placedOn.get(Cloister.class).getOrElse(0);
            pr -= OPEN_CLOISTER_PENALTY[open];
            open = placedOn.get(Farm.class).getOrElse(0);
            pr -= OPEN_FARM_PENALTY[open];
            r += ptsforPlayer(player, pr);
        }
        return r;
    }

    private double rateUnfinishedFeatures() {
        double r = 0.0;

        for (Completable completable : state.getFeatures(Completable.class)) {
            CompletableRanking cr = new CompletableRanking(state, completable);
            double fr = 0.0;

            if (completable instanceof CompletableFeature) {
                CompletableFeature<?> cf = (CompletableFeature<?>) completable;
                for (Edge edge : cf.getOpenEdges()) {
                    edges.put(edge, cr);
                }
            }

            double prob = countCompleteProbability(completable);
            cr.setProbability(prob);

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
                            fr -= penalty._1;
                        } else {
                            fr += penalty._2;
                        }
                    }
                }
            }

            double points = getCompletablePoints(cr);
            for (Player player : cr.getOwners()) {
                fr += ptsforPlayer(player, points);
            }

//            if (logger.isDebugEnabled()) {
//                logger.debug(String.format("    > %s  %8.5f", completable, fr));
//            }
            r += fr;
        }

        for (Farm farm : state.getFeatures(Farm.class)) {
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

        return r;
    }

    private double rateMeeples() {
        double r = 0.0;
        for (Player player : state.getPlayers().getPlayers()) {
            double q = player == me ? 1.0 : -1.0;
            int inSupply = 0;
            for (Follower f : player.getFollowers(state).filter(f -> f.isInSupply(state))) {
                //instanceof cannot be used because of Phantom
                if (f.getClass().equals(SmallFollower.class)) {
                    r += q * 0.15;
                } else {
                    r += q * 0.25;
                }
                inSupply += 1;
            }
            if (inSupply == 0) {
                r += ptsforPlayer(player, 1.5);
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
            r -= ptsforPlayer(t._1.getPlayer(), 3 - dist);
        }
        return r;
    }

    private double rateBoardShape() {
        return 0.0001 * state.getAdjacentTiles2(lastPlaced.getPosition()).size();
    }

    private HashMap<Player, Integer> getCombinedPowers(CompletableRanking r1, CompletableRanking r2) {
        HashMap<Player, Integer> combinedPowers = r1.getPowers();
        for (Tuple2<Player, Integer> t : r2.getPowers()) {
            int p = combinedPowers.get(t._1).getOrElse(0);
            combinedPowers = combinedPowers.put(t._1, p + t._2);
        }
        return combinedPowers;
    }

    private double rateConnection(Edge e1, Edge e2, double prob) {
        CompletableRanking r1 = edges.get(e1);
        CompletableRanking r2 = edges.get(e2);
        if (r1 == null || r2 == null || !r1.getFeature().getClass().equals(r2.getFeature().getClass())) {
            return 0.0;
        }
        if (r2.getOwnersPower() > r1.getOwnersPower()) {
            CompletableRanking tmp = r2;
            r2 = r1;
            r1 = tmp;
        }

        if (r1.getOwnersPower() == 0) {
            return 0.0;
        }

        HashMap<Player, Integer> combinedPowers = getCombinedPowers(r1, r2);
        int combinedMaxPower = combinedPowers.values().max().getOrElse(0);
        Set<Player> combinedOwners = combinedPowers.keySet().filter(p -> combinedPowers.get(p).get() == combinedMaxPower);


        Set<Player> loss1 = r1.getOwners().diff(combinedOwners);
        Set<Player> loss2 = r2.getOwners().diff(combinedOwners);
        Set<Player> gain1 = combinedOwners.diff(r1.getOwners());
        Set<Player> gain2 = combinedOwners.diff(r2.getOwners());
        double featurePoints1 = getCompletablePoints(r1);
        double featurePoints2 = getCompletablePoints(r2);
        double points1 = 0.5 * prob * featurePoints1;
        double points2 = 0.5 * prob * featurePoints2;

        double r = 0.0;
        for (Player p : loss1) r -= ptsforPlayer(p, points1);
        for (Player p : loss2) r -= ptsforPlayer(p, points2);
        for (Player p : gain1) r += ptsforPlayer(p, points1);
        for (Player p : gain2) r += ptsforPlayer(p, points2);
        return r;
    }

    private double rateConnections() {
        double r = 0.0;
        for (Tuple2<Position, Double> pp : positionProbability) {
            if (pp._2 < 0.55) continue;
            Position pos = pp._1;
            for (Location loc : Location.SIDES) {
                r += rateConnection(
                   new Edge(pos, loc),
                   new Edge(pos, loc.rotateCW(Rotation.R90)),
                   pp._2
                );
            }
        }
        return r;
    }

}