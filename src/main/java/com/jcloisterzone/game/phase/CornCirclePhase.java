package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.*;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.capability.CornCircleCapability;
import com.jcloisterzone.game.capability.CornCircleCapability.CornCircleModifier;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.*;
import com.jcloisterzone.io.message.CornCircleRemoveOrDeployMessage.CornCircleOption;
import com.jcloisterzone.io.message.ReturnMeepleMessage.ReturnMeepleSource;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.reducers.UndeployMeeple;
import io.vavr.Tuple2;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

public class CornCirclePhase extends Phase {

    public CornCirclePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    private Class<? extends Feature> getCornType(GameState state) {
        PlacedTile placedTile = state.getLastPlaced();

        return placedTile.getTile().getTileModifiers()
        		.find(m -> m instanceof CornCircleModifier)
        		.map(m -> ((CornCircleModifier)m).getFeatureType())
        		.getOrNull();
    }


    @Override
    public StepResult enter(GameState state) {
        Class<? extends Feature> cornType = getCornType(state);

        // if no follower is deployed corn circle has not effective result for both choices, we can skip it
        if (cornType == null || state.getDeployedMeeples().isEmpty()) {
            return next(state);
        }

        CornCircleSelectDeployOrRemoveAction action = new CornCircleSelectDeployOrRemoveAction(cornType);
        ActionsState as = new ActionsState(state.getTurnPlayer(), action, false);
        return promote(state.setPlayerActions(as));
    }

    @PhaseMessageHandler
    public StepResult handleCornCircleRemoveOrDeployMessage(GameState state, CornCircleRemoveOrDeployMessage msg) {
        state = state.setCapabilityModel(CornCircleCapability.class, msg.getValue());

        Player player = state.getTurnPlayer().getNextPlayer(state);
        return createAction(state, player);
    }

    private boolean isLast(GameState state, Player player) {
        return state.getTurnPlayer().equals(player);
    }

    private StepResult endPhase(GameState state) {
        state = clearActions(state);
        return next(state);
    }

    private StepResult nextCornPlayer(GameState state, Player player) {
        if (isLast(state, player)) {
            return endPhase(state);
        } else {
            return createAction(state, player.getNextPlayer(state));
        }
    }

    private StepResult createAction(GameState state, Player player) {
        CornCircleOption option = state.getCapabilityModel(CornCircleCapability.class);
        Class<? extends Feature> cornType = getCornType(state);

        Stream<Tuple2<Meeple, FeaturePointer>> followers = Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> t._1 instanceof Follower)
            .filter(t -> t._1.getPlayer().equals(player))
            .filter(t -> cornType.isInstance(state.getFeature(t._2)));

        if (followers.isEmpty()) {
            return nextCornPlayer(state, player);
        }

        Vector<PlayerAction<?>> actions = null;

        switch (option) {
        case DEPLOY:
            Vector<Class<? extends Meeple>> meepleTypes = Vector.of(SmallFollower.class, BigFollower.class, Phantom.class, Ringmaster.class);
            if (!cornType.equals(Field.class)) {
                meepleTypes = meepleTypes.append(Wagon.class);
            }
            if (cornType.equals(City.class)) {
                meepleTypes = meepleTypes.append(Mayor.class);
            }

            Vector<Meeple> availMeeples = player.getMeeplesFromSupply(state, meepleTypes);
            if (availMeeples.isEmpty()) {
                return nextCornPlayer(state, player);
            }

            Set<FeaturePointer> deployOptions = followers.map(Tuple2::_2).toSet();
            actions = availMeeples.map(meeple ->
                new MeepleAction(meeple, deployOptions)
            );
            break;
        case REMOVE:
            Set<MeeplePointer> removeOptions = followers.map(MeeplePointer::new).toSet();
            actions = Vector.of(
                new ReturnMeepleAction(removeOptions, ReturnMeepleSource.CORN_CIRCLE)
            );
            break;
        }

        return promote(state.setPlayerActions(
            new ActionsState(player, actions, option == CornCircleOption.DEPLOY)
        ));
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        CornCircleOption option = state.getCapabilityModel(CornCircleCapability.class);
        if (option != CornCircleOption.DEPLOY) {
            throw new IllegalStateException();
        }

        Player player = state.getActivePlayer();
        FeaturePointer fp = msg.getPointer();
        Meeple m = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());
        state = (new DeployMeeple(m, fp)).apply(state);

        return promote(state.setPlayerActions(new ActionsState(player, new ConfirmAction(), false)));
    }

    @PhaseMessageHandler
    public StepResult handleReturnMeeple(GameState state, ReturnMeepleMessage msg) {
        CornCircleOption option = state.getCapabilityModel(CornCircleCapability.class);
        if (option != CornCircleOption.REMOVE) {
            throw new IllegalStateException();
        }

        MeeplePointer ptr = msg.getPointer();
        if (msg.getSource() != ReturnMeepleSource.CORN_CIRCLE) {
            throw new IllegalStateException();
        }

        Player player = state.getActivePlayer();
        Meeple meeple = state.getDeployedMeeples().find(m -> ptr.match(m._1)).map(t -> t._1)
            .getOrElseThrow(() -> new IllegalArgumentException("Pointer doesn't match any meeple"));
        state = (new UndeployMeeple(meeple, true)).apply(state);

        return promote(state.setPlayerActions(new ActionsState(player, new ConfirmAction(), false)));
    }

    @PhaseMessageHandler
    public StepResult handleCommit(GameState state, CommitMessage msg) {
        return nextCornPlayer(state, state.getActivePlayer());
    }

    @PhaseMessageHandler
    @Override
    public StepResult handlePass(GameState state, PassMessage msg) {
        CornCircleOption option = state.getCapabilityModel(CornCircleCapability.class);
        if (option != CornCircleOption.DEPLOY) {
            throw new IllegalStateException();
        }

        Player player = state.getActivePlayer();
        return nextCornPlayer(state, player);
    }
}
