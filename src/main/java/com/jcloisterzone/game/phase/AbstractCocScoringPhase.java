package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.DeployMeepleMessage;
import com.jcloisterzone.io.message.PassMessage;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.DeployMeeple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

import java.util.function.Function;

public abstract class AbstractCocScoringPhase extends Phase {

    public AbstractCocScoringPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    protected abstract Function<Feature, Boolean> getAllowedFeaturesFilter(GameState state);
    protected abstract StepResult nextPlayer(GameState state, Player player, boolean actionUsed);
    protected abstract List<Location> getValidQuarters(GameState state);

    protected StepResult endPhase(GameState state) {
        state = clearActions(state);
        return next(state);
    }

    private Class<? extends Scoreable> getFeatureTypeForLocation(Location loc) {
        if (loc == Location.QUARTER_CASTLE) return City.class;
        if (loc == Location.QUARTER_BLACKSMITH) return Road.class;
        if (loc == Location.QUARTER_CATHEDRAL) return Monastery.class;
        if (loc == Location.QUARTER_MARKET) return Field.class;
        throw new IllegalArgumentException("Illegal location " + loc);
    }

    protected StepResult processPlayer(GameState state, Player player) {
        FeaturePointer countFp = state.getNeutralFigures().getCountDeployment();
        Function<Feature, Boolean> filter = getAllowedFeaturesFilter(state);

        Vector<MeepleAction> actions = getValidQuarters(state)
            .filter(quarter -> quarter != countFp.getLocation())
            .flatMap(quarter -> {
                Set<FeaturePointer> options = state.getFeatures(getFeatureTypeForLocation(quarter))
                    .filter(filter::apply)
                    .flatMap(Feature::getPlaces)
                    .toSet()
                    .filter(tp -> {
                        for (Capability<?> cap : state.getCapabilities().toSeq()) {
                            if (!cap.isMeepleDeploymentAllowed(state, tp.getPosition())) return false;
                        }
                        return true;
                    });

                if (options.isEmpty()) {
                    return List.empty();
                }

                Position quarterPosition = state.getPlacedTiles().filter(t -> t._2.getTile().getId().equals(CountCapability.QUARTER_ACTION_TILE_ID)).get()._1;
                FeaturePointer quarterFp = new FeaturePointer(quarterPosition, Quarter.class, quarter);

                return state.getDeployedMeeples()
                    .filter(t -> t._2.getLocation() == quarter)   // is deployed on quarter
                    .map(Tuple2::_1)
                    .filter(m -> m.getPlayer().equals(player))    // and is owned by active player
                    .groupBy(Object::getClass)                    // for each meeple class create action ...
                    .values()
                    .map(Seq::get)
                    .map(m -> new MeepleAction(m, options, quarterFp));
            })
            .toVector();

        if (actions.isEmpty()) {
            return null;
        }

        ActionsState as = new ActionsState(player, Vector.narrow(actions), true);
        as = as.mergeMeepleActions();
        return promote(state.setPlayerActions(as));
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Player player = state.getActivePlayer();
        Follower follower = player.getFollowers(state).find(f -> f.getId().equals(msg.getMeepleId())).get();
        state = (new DeployMeeple(follower, fp)).apply(state);
        return nextPlayer(state, player, true);
    }

    @Override
    @PhaseMessageHandler
    public StepResult handlePass(GameState state, PassMessage msg) {
        Player player = state.getActivePlayer();
        return nextPlayer(state, player, false);
    }
}
