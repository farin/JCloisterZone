package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

public abstract class AbstractCocScoringPhase extends Phase {

    public AbstractCocScoringPhase(Config config, Random random) {
        super(config, random);
    }

    private boolean isLast(GameState state, Player player) {
        return state.getTurnPlayer().equals(player);
    }

    private StepResult endPhase(GameState state) {
        state = clearActions(state);
        return next(state);
    }

    private StepResult nextPlayer(GameState state, Player player) {
        if (isLast(state, player)) {
            return endPhase(state);
        } else {
            return processPlayer(state, player.getNextPlayer(state));
        }
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer().getNextPlayer(state);
        return processPlayer(state, player);
    }

    private Class<? extends Scoreable> getFeatureTypeForLocation(Location loc) {
        if (loc == Location.QUARTER_CASTLE) return City.class;
        if (loc == Location.QUARTER_BLACKSMITH) return Road.class;
        if (loc == Location.QUARTER_CATHEDRAL) return Cloister.class;
        if (loc == Location.QUARTER_MARKET) return Farm.class;
        throw new IllegalArgumentException("Illegal locaion " + loc);
    }

    protected abstract List<Location> getScoredQuarters();

    private StepResult processPlayer(GameState state, Player player) {
        FeaturePointer countFp = state.getNeutralFigures().getCountDeployment();
        Position lastPlacedPos = state.getLastPlaced().getPosition();

        // TODO derive final phase including farms
        Vector<MeepleAction> actions = getScoredQuarters()
            .filter(quarter -> quarter != countFp.getLocation())
            .flatMap(quarter -> {
                Set<FeaturePointer> options = state.getFeatures(getFeatureTypeForLocation(quarter))
                    .filter(f -> f instanceof Farm || ((Completable) f).isCompleted(state))
                    .flatMap(f -> {
                        List<FeaturePointer> places = f.getPlaces();
                        if (f instanceof Farm || places.find(p -> p.getPosition().equals(lastPlacedPos)).isDefined()) {
                            //feature lays on last placed tile -> is finished this turn
                            return places;
                        } else {
                            return List.empty();
                        }
                    })
                    .toSet();

                if (options.isEmpty()) {
                    return List.empty();
                }

                return state.getDeployedMeeples()
                    .filter(t -> t._2.getLocation() == quarter)   // is deployed on quarter
                    .map(Tuple2::_1)
                    .filter(m -> m.getPlayer().equals(player))    // and is owned by active player
                    .groupBy(Object::getClass)					  // for each meeple class create action ...
                    .values()
                    .map(Seq::get)
                    .map(m -> new MeepleAction(m, options));
            })
            .toVector();

        if (actions.isEmpty()) {
            return nextPlayer(state, player);
        }

        ActionsState as = new ActionsState(player, Vector.narrow(actions), true);
        as = as.mergeMeepleActions();
        return promote(state.setPlayerActions(as));
    }

    @Override
    @PhaseMessageHandler
    public StepResult handlePass(GameState state, PassMessage msg) {
        Player player = state.getActivePlayer();
        return nextPlayer(state, player);
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Player player = state.getActivePlayer();
        Follower follower = player.getFollowers(state).find(f -> f.getId().equals(msg.getMeepleId())).get();

        state = (new DeployMeeple(follower, fp)).apply(state);
        return processPlayer(state, player);
    }

}
