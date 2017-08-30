package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.CornCircleSelectDeployOrRemoveAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.capability.CornCircleCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.reducers.UndeployMeeple;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCicleOption;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;

import io.vavr.Tuple2;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

@RequiredCapability(CornCircleCapability.class)
public class CornCirclePhase extends Phase {

    public CornCirclePhase(Config config, Random random) {
        super(config, random);
    }

    private Class<? extends Feature> getCornType(GameState state) {
        PlacedTile placedTile = state.getLastPlaced();
        return  placedTile.getTile().getCornCircle();
    }


    @Override
    public StepResult enter(GameState state) {
        Class<? extends Feature> cornType = getCornType(state);

        // if no follower is deployed corn circle has not effective result for both choices, we can skip it
        if (cornType == null || state.getDeployedMeeples().isEmpty()) {
            return next(state);
        }

        CornCircleSelectDeployOrRemoveAction action = new CornCircleSelectDeployOrRemoveAction();
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
        CornCicleOption option = state.getCapabilityModel(CornCircleCapability.class);
        Class<? extends Feature> cornType = getCornType(state);

        Stream<Tuple2<Meeple, FeaturePointer>> meeples = Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> t._1.getPlayer().equals(player))
            .filter(t -> cornType.isInstance(state.getFeature(t._2)));

        if (meeples.isEmpty()) {
            return nextCornPlayer(state, player);
        }

        Vector<PlayerAction<?>> actions = null;

        switch (option) {
        case DEPLOY:
            Vector<Class<? extends Meeple>> meepleTypes = Vector.of(SmallFollower.class, BigFollower.class, Phantom.class);
            if (!cornType.equals(Farm.class)) {
                meepleTypes = meepleTypes.append(Wagon.class);
            }
            if (cornType.equals(City.class)) {
                meepleTypes = meepleTypes.append(Mayor.class);
            }

            Vector<Meeple> availMeeples = player.getMeeplesFromSupply(state, meepleTypes);
            if (availMeeples.isEmpty()) {
                return nextCornPlayer(state, player);
            }

            Set<FeaturePointer> deployOptions = meeples.map(Tuple2::_2).toSet();
            actions = availMeeples.map(meeple ->
                new MeepleAction(meeple.getClass(), deployOptions)
            );
            break;
        case REMOVE:
            Set<MeeplePointer> removeOptions = meeples.map(t -> new MeeplePointer(t._2, t._1.getId())).toSet();
            actions = Vector.of(
                new ReturnMeepleAction(removeOptions, ReturnMeepleSource.CORN_CIRCLE)
            );
            break;
        }

        return promote(state.setPlayerActions(
            new ActionsState(player, actions, option == CornCicleOption.DEPLOY)
        ));
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        CornCicleOption option = state.getCapabilityModel(CornCircleCapability.class);
        if (option != CornCicleOption.DEPLOY) {
            throw new IllegalStateException();
        }

        Player player = state.getActivePlayer();
        FeaturePointer fp = msg.getPointer();
        Meeple m = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());
        state = (new DeployMeeple(m, fp)).apply(state);

        return nextCornPlayer(state, player);
    }

    @PhaseMessageHandler
    public StepResult handleReturnMeeple(GameState state, ReturnMeepleMessage msg) {
        CornCicleOption option = state.getCapabilityModel(CornCircleCapability.class);
        if (option != CornCicleOption.REMOVE) {
            throw new IllegalStateException();
        }

        MeeplePointer ptr = msg.getPointer();
        if (msg.getSource() != ReturnMeepleSource.CORN_CIRCLE) {
            throw new IllegalStateException();
        }

        Player player = state.getActivePlayer();
        Meeple meeple = state.getDeployedMeeples().find(m -> ptr.match(m._1)).map(t -> t._1)
            .getOrElseThrow(() -> new IllegalArgumentException("Pointer doesn't match any meeple"));
        state = (new UndeployMeeple(meeple)).apply(state);
        return nextCornPlayer(state, player);
    }

    @PhaseMessageHandler
    @Override
    public StepResult handlePass(GameState state, PassMessage msg) {
        CornCicleOption option = state.getCapabilityModel(CornCircleCapability.class);
        if (option != CornCicleOption.DEPLOY) {
            throw new IllegalStateException();
        }

        Player player = state.getActivePlayer();
        return nextCornPlayer(state, player);
    }
}
