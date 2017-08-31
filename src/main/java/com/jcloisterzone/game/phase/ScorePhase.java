package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.ScoreCompletable;
import com.jcloisterzone.reducers.ScoreFarm;
import com.jcloisterzone.reducers.ScoreFarmWhenBarnIsConnected;
import com.jcloisterzone.reducers.UndeployMeeples;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.control.Option;


public class ScorePhase extends Phase {

    private java.util.Map<Completable, Integer> completedMutable = new java.util.HashMap<>();

    public ScorePhase(Config config, Random random) {
        super(config, random);
    }

    private GameState scoreCompletedOnTile(GameState state, PlacedTile tile) {
        for (Tuple2<Location, Completable> t : state.getTileFeatures2(tile.getPosition(), Completable.class)) {
            state = scoreCompleted(state, t._2, tile);
        }
        return state;
    }

    private GameState scoreCompletedNearAbbey(GameState state, Position pos) {
        for (Tuple2<Location, PlacedTile> t : state.getAdjacentTiles2(pos)) {
            PlacedTile pt = t._2;
            Feature feature = state.getFeaturePartOf(new FeaturePointer(pt.getPosition(), t._1.rev()));
            if (feature instanceof Completable) {
                state = scoreCompleted(state, (Completable) feature, null);
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
                state = (new ScoreFarm(placedBarnFarm)).apply(state);
                state = (new UndeployMeeples(placedBarnFarm)).apply(state);
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
                state = (new UndeployMeeples(farm)).apply(state);
            }
        }

        state = scoreCompletedOnTile(state, lastPlaced);
        if (lastPlaced.getTile().isAbbeyTile()) {
            state = scoreCompletedNearAbbey(state, pos);
        }

        if (state.getCapabilities().contains(TunnelCapability.class)) {
            GameState _state = state;
            List<Feature> tunnelModified = state.getCurrentTurnEvents()
                .filter(Predicates.instanceOf(TokenPlacedEvent.class))
                .map(ev -> (TokenPlacedEvent) ev)
                .filter(ev -> ev.getToken().isTunnel())
                .map(ev -> _state.getFeature((FeaturePointer) ev.getPointer()));
            assert tunnelModified.size() <= 1;

            for (Feature road : tunnelModified) {
                state = scoreCompleted(state, (Completable) road, null);
            }
        }

        Set<Position> neighbourPositions = state.getAdjacentAndDiagonalTiles2(pos)
            .map(pt -> pt._2.getPosition()).toSet();

        for (Cloister cloister : state.getFeatures(Cloister.class)) {
            if (neighbourPositions.contains(cloister.getPlace().getPosition())) {
                state = scoreCompleted(state, cloister, null);
            }
        }

        HashMap<Completable, Integer> completed = HashMap.ofAll(completedMutable);
        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onCompleted(state, completed);
        }

        if (state.getCapabilities().contains(GoldminesCapability.class)) {
            //gldCap.awardGoldPieces();
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
        return LinkedHashMap.narrow(
         state.getDeployedMeeples()
           .filter((m, fp) -> m instanceof Wagon)
        );
    }

    private GameState scoreCompleted(GameState state, Completable completable, PlacedTile triggerBuilderForPlaced) {
        if (triggerBuilderForPlaced != null && state.getCapabilities().contains(BuilderCapability.class)) {
            Player player = state.getTurnPlayer();
            GameState _state = state;
            Option<Meeple> builder = completable
                .getMeeples(state)
                .find(m -> {
                    return m instanceof Builder
                        && m.getPlayer().equals(player)
                        && !m.getPosition(_state).equals(triggerBuilderForPlaced.getPosition());
                });
            if (!builder.isEmpty()) {
                state = state.getCapabilities().get(BuilderCapability.class).useBuilder(state);
            }
        }

        if (completable.isCompleted(state) && !completedMutable.containsKey(completable)) {
            int points = completable.getPoints(state);

            completedMutable.put(completable, points);

            state = (new ScoreCompletable(completable, points)).apply(state);
            state = (new UndeployMeeples(completable)).apply(state);
        }

        return state;
    }

}
