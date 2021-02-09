package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.ShortEdge;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.TokenPlacedEvent;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.ScoreCompletable;
import com.jcloisterzone.reducers.ScoreFarm;
import com.jcloisterzone.reducers.ScoreFarmWhenBarnIsConnected;
import com.jcloisterzone.reducers.UndeployMeeples;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.*;


public class ScoringPhase extends Phase {

    private java.util.Map<Completable, ScoreFeatureReducer> completedMutable = new java.util.HashMap<>();

    public ScoringPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    private GameState scoreCompletedOnTile(GameState state, PlacedTile tile) {
        for (Tuple2<Location, Completable> t : state.getTileFeatures2(tile.getPosition(), Completable.class)) {
            state = scoreCompleted(state, t._2);
        }
        return state;
    }

    private GameState scoreClosedByFerries(GameState state) {
        /*
            A scoring is handled by placement itself
            must take care about B & C


           -A - B    -->     -A  B        B (disconnected) is now completed must be score
                C              \ C

           -A - B    -->     -A  /B
                C                \C       C (connected) must me scored

        */
        for (Tuple2<Position, Tuple2<Location, Location>> t : state.getCapabilityModel(FerriesCapability.class).getMovedFerries()) {
            Location from = t._2._1;
            Location to = t._2._2;

            // disconnected
            List<FeaturePointer> affected = from.subtract(to).splitToSides()
                .map(loc -> new FeaturePointer(t._1, loc));
            // connected (merge only first side is enough, it's connected, sides must belong to same road
            affected = affected.append(new FeaturePointer(t._1, to.subtract(from).splitToSides().get()));

            for (FeaturePointer fp : affected) {
                Road road = (Road) state.getFeature(fp);
                state = scoreCompleted(state, road);
            }
        }
        return state;
    }

    private GameState scoreCompletedNearAbbey(GameState state, Position pos) {
        for (Tuple2<Location, PlacedTile> t : state.getAdjacentTiles2(pos)) {
            PlacedTile pt = t._2;
            FeaturePointer fp = new FeaturePointer(pt.getPosition(), t._1.rev());
            Feature feature = state.getFeaturePartOf(fp);
            if (feature instanceof Completable) {
                state = scoreCompleted(state, (Completable) feature);
            }
            if (feature instanceof City) {
                // also check if second city on multi edge is completed
                City city = (City) feature;
                ShortEdge edge = new ShortEdge(pos, pt.getPosition());
                Tuple2<ShortEdge, FeaturePointer> multiEdge = city.getMultiEdges().find(me -> me._1.equals(edge)).getOrNull();
                if (multiEdge != null) {
                    City another = (City) state.getFeature(multiEdge._2);
                    state = scoreCompleted(state, another);
                }
            }
        }
        return state;
    }

    @Override
    public StepResult enter(GameState state) {
        PlacedTile lastPlaced = state.getLastPlaced();
        Position pos = lastPlaced.getPosition();

        Map<Wagon, FeaturePointer> deployedWagonsBefore = getDeployedWagons(state);

        if (state.getCapabilities().contains(BarnCapability.class)) {
            FeaturePointer placedBarnPtr = state.getCapabilityModel(BarnCapability.class);
            Farm placedBarnFarm = placedBarnPtr == null ? null : (Farm) state.getFeature(placedBarnPtr);
            if (placedBarnFarm != null) {
                //ScoreFeature is scoring just followers!
                state = (new ScoreFarm(placedBarnFarm, false)).apply(state);
                state = (new UndeployMeeples(placedBarnFarm, false)).apply(state);
            }

            GameState _state = state;
            for (Farm farm : state.getTileFeatures2(pos)
                .map(Tuple2::_2)
                .filter(f -> f != placedBarnFarm)
                .filter(Predicates.instanceOf(Farm.class))
                .map(f -> (Farm) f)
                .filter(farm -> farm.getSpecialMeeples(_state)
                    .find(Predicates.instanceOf(Barn.class))
                    .isDefined()
                )) {
                state = (new ScoreFarmWhenBarnIsConnected(farm)).apply(state);
                state = (new UndeployMeeples(farm, false)).apply(state);
            }
        }

        state = scoreCompletedOnTile(state, lastPlaced);
        if (AbbeyCapability.isAbbey(lastPlaced.getTile())) {
            state = scoreCompletedNearAbbey(state, pos);
        }

        if (state.getCapabilities().contains(FerriesCapability.class)) {
            state = scoreClosedByFerries(state);
        }

        if (state.getCapabilities().contains(TunnelCapability.class)) {
            GameState _state = state;
            List<Feature> tunnelModified = state.getCurrentTurnEvents()
                .filter(Predicates.instanceOf(TokenPlacedEvent.class))
                .map(ev -> (TokenPlacedEvent) ev)
                .filter(ev -> ev.getToken() instanceof Tunnel)
                .map(ev -> _state.getFeature((FeaturePointer) ev.getPointer()));
            assert tunnelModified.size() <= 1;

            for (Feature road : tunnelModified) {
                state = scoreCompleted(state, (Completable) road);
            }
        }

        Set<Position> neighbourPositions = state.getAdjacentAndDiagonalTiles2(pos)
            .map(pt -> pt._2.getPosition()).toSet();


        for (CloisterLike cloister : state.getFeatures(CloisterLike.class)) {
            if (neighbourPositions.contains(cloister.getPosition())) {
                state = scoreCompleted(state, cloister);
            }
        }

        // return all meeples when all is scored, important for scoring darmstadt churchs
        for (Completable completable : completedMutable.keySet()) {
            state = (new UndeployMeeples(completable, false)).apply(state);
        }

        CastleCapability castleCap = state.getCapabilities().get(CastleCapability.class);
        HashMap<Completable, ScoreFeatureReducer> completed = HashMap.ofAll(completedMutable);
        HashMap<Scoreable, ScoreFeatureReducer> scored = HashMap.narrow(completed);
        if (castleCap != null) {
            Tuple2<GameState, Map<Castle, ScoreFeatureReducer>> castleRes = castleCap.scoreCastles(state, completed);
            state = castleRes._1;
            scored = scored.merge(castleRes._2);
        }

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onTurnScoring(state, scored);
        }

        if (!deployedWagonsBefore.isEmpty()) {
            Set<Wagon> deployedWagonsAfter = getDeployedWagons(state).keySet();
            Set<Wagon> returnedVagons = deployedWagonsBefore.keySet().diff(deployedWagonsAfter);

            Queue<Tuple2<Wagon, FeaturePointer>> model = state.getPlayers()
                .getPlayersBeginWith(state.getTurnPlayer())
                .map(p -> returnedVagons.find(w -> w.getPlayer().equals(p)).getOrNull())
                .filter(Predicates.isNotNull())
                .map(w -> new Tuple2<>(w, deployedWagonsBefore.get(w).get()))
                .toQueue();
            state = state.setCapabilityModel(WagonCapability.class, model);
        }

        completedMutable.clear();
        return next(state);
    }

    private Map<Wagon, FeaturePointer> getDeployedWagons(GameState state) {
        return state.getDeployedMeeples()
           .filter((m, fp) -> m instanceof Wagon)
           .mapKeys(m -> (Wagon) m);
    }

    private GameState scoreCompleted(GameState state, Completable completable) {
        if (completable.isCompleted(state) && !completedMutable.containsKey(completable)) {
            /*
              When playing with German / Dutch & Belgian Monasteries: Because an abbot scores only at the end of the game,
              and this monastery is never considered completed, a monastery with an abbot will not score points for a follower in a castle. (5/2014)

              Also this complicates with flying machines.
            */
            if (completable instanceof Cloister && ((Cloister) completable).isMonastery()) {
                Cloister monastery = (Cloister) completable;
                List<Tuple2<Meeple, FeaturePointer>> meeples = monastery.getMeeplesIncludingMonastery2(state).toList();
                if (meeples.size() > 0 && meeples.filter(t -> t._2.getLocation() == Location.CLOISTER).size() == 0) {
                    // only abbots on monastery
                    return state;
                }
            }
            ScoreCompletable scoreReducer = new ScoreCompletable(completable, false);
            state = scoreReducer.apply(state);


            completedMutable.put(completable, scoreReducer);
        }
        return state;
    }

}
