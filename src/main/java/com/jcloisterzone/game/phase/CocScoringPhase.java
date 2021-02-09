package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleDeployed;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.CommitMessage;
import com.jcloisterzone.io.message.PassMessage;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.io.message.DeployMeepleMessage;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.List;

import java.util.function.Function;

@RequiredCapability(CountCapability.class)
public class CocScoringPhase extends AbstractCocScoringPhase {

    public CocScoringPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        return nextPlayer(state, state.getTurnPlayer(), false);
    }

    @Override
    @PhaseMessageHandler
    public StepResult handlePass(GameState state, PassMessage msg) {
        Player player = state.getActivePlayer();
        if (player.equals(state.getTurnPlayer())) {
            return endPhase(state);
        }
        return super.handlePass(state, msg);
    }

    @PhaseMessageHandler
    public StepResult handleCommit(GameState state, CommitMessage msg) {
        return this.handlePass(state, null);
    }

    @Override
    protected StepResult nextPlayer(GameState state, Player player, boolean actionUsed) {
        Player p = player;
        if (!actionUsed) {
            p = player.getNextPlayer(state);
        }
        while (true) {
            StepResult res = processPlayer(state, p);
            if (res != null) {
                return res;
            }
            if (actionUsed && p == player) {
                return promote(state.setPlayerActions(new ActionsState(player, new ConfirmAction(), false)));
            }

            if (p.equals(state.getTurnPlayer())) {
                return endPhase(state);
            }
            p = p.getNextPlayer(state);
        }
    }

    @Override
    protected List<Location> getValidQuerters(GameState state) {
        return Location.QUARTERS;
    }

    @Override
    protected Function<Feature, Boolean> getAllowedFeaturesFilter(GameState state) {
        PlacedTile lastPlaced = state.getLastPlaced();
        Position lastPlacedPos = lastPlaced.getPosition();

        java.util.Set<Completable> justPlacedAbbeyAdjacent = new java.util.HashSet<>();
        if (AbbeyCapability.isAbbey(lastPlaced.getTile())) {
            for (Tuple2<Location, PlacedTile> t : state.getAdjacentTiles2(lastPlacedPos)) {
                PlacedTile pt = t._2;
                Feature feature = state.getFeaturePartOf(new FeaturePointer(pt.getPosition(), t._1.rev()));
                if (feature instanceof Completable) {
                    justPlacedAbbeyAdjacent.add((Completable) feature);
                }
            }
        }

        java.util.HashSet<Feature> barnInvolvedFarms = new java.util.HashSet<>();

        if (state.getCapabilities().contains(BarnCapability.class)) {
            FeaturePointer placedBarnPtr = state.getCapabilityModel(BarnCapability.class);
            Farm placedBarnFarm = placedBarnPtr == null ? null : (Farm) state.getFeature(placedBarnPtr);
            if (placedBarnFarm != null) {
                barnInvolvedFarms.add(placedBarnFarm);

            }

            Position pos = lastPlaced.getPosition();
            state.getTileFeatures2(pos)
                .map(Tuple2::_2)
                .filter(f -> f != placedBarnFarm)
                .filter(Predicates.instanceOf(Farm.class))
                .map(f -> (Farm) f)
                .filter(farm -> farm.getSpecialMeeples(state)
                    .find(Predicates.instanceOf(Barn.class))
                    .isDefined()
                )
                .filter(farm -> !farm.getFollowers(state).isEmpty())  //must contains at least one follower
                .forEach(farm -> barnInvolvedFarms.add(farm));
        }

        return f -> {
            if (f instanceof Farm) {
                return barnInvolvedFarms.contains(f);
            }
            if (f instanceof Completable) {
                Completable completable = (Completable) f;
                if (!completable.isCompleted(state)) {
                    return false;
                }

                if (justPlacedAbbeyAdjacent.contains(f)) {
                    return true;
                }

                //feature lays on last placed tile -> is finished this turn
                if (f.getPlaces().find(p -> p.getPosition().equals(lastPlacedPos)).isDefined()) {
                    return true;
                }

                if (f instanceof Cloister) { // Cloister, not CloisterLike
                    Position cloisterPos = ((Cloister) f).getPosition();
                    if (!Position.ADJACENT_AND_DIAGONAL
                        .map(t -> cloisterPos.add(t._2))
                        .filter(p -> p.equals(lastPlacedPos))
                        .isEmpty()) {
                        return true;
                    }
                }

                return false;
            }
            throw new UnsupportedOperationException();
        };
    }
}
