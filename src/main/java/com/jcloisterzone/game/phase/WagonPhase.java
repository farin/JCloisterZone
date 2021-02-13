package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.RussianPromosTrapCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.CommitMessage;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.io.message.DeployMeepleMessage;
import com.jcloisterzone.io.message.PassMessage;
import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class WagonPhase extends Phase {

    public WagonPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        Queue<Tuple2<Wagon, FeaturePointer>> model = state.getCapabilityModel(WagonCapability.class);
        while (!model.isEmpty()) {
            Tuple2<Tuple2<Wagon, FeaturePointer>, Queue<Tuple2<Wagon, FeaturePointer>>> dequeueTuple = model.dequeue();
            model = dequeueTuple._2;
            state = state.setCapabilityModel(WagonCapability.class, model);
            Tuple2<Wagon, FeaturePointer> item = dequeueTuple._1;
            Wagon wagon = item._1;

            Feature feature = state.getFeature(item._2);
            if (feature instanceof Completable) { // skip Castle
                GameState _state = state;
                Set<FeaturePointer> options = getAdjacentFeatures(state, (Completable)feature, item._2)
                        .filter(t -> {
                            Feature f = t._2;
                            if (f instanceof Castle) {
                                Castle castle = (Castle) f;
                                return !castle.isOccupied(_state);
                            }
                            if (f instanceof Completable) {
                                Completable nei = (Completable) f;
                                if ((f instanceof  Structure) && wagon.isDeploymentAllowed(_state, t._1, (Structure) f) != DeploymentCheckResult.OK) {
                                    return false;
                                }
                                return !nei.isCompleted(_state) && !nei.isOccupied(_state);
                            }
                            return false; // eg f == null
                        })
                        .map(Tuple2::_1)
                        .filter(fp -> {
                            for (Capability<?> cap : _state.getCapabilities().toSeq()) {
                                if (!cap.isMeepleDeploymentAllowed(_state, fp.getPosition())) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .toSet();

                if (!options.isEmpty()) {
                    PlayerAction<?> action = new MeepleAction(wagon, options);
                    state = state.setPlayerActions(
                        new ActionsState(wagon.getPlayer(), action, true)
                    );
                    return promote(state);
                }
            }
        }
        return next(state);
    }

    private Stream<Tuple2<FeaturePointer, Feature>> getAdjacentFeatures(GameState state, Completable feature, FeaturePointer source) {
        if ("C1".equals(state.getStringRule(Rule.WAGON_MOVE))) {
            return Stream.ofAll(feature.getNeighboring())
                    .map(fp -> new Tuple2<>(fp, state.getFeature(fp)));
        } else {
            Position sourcePos = source.getPosition();
            return Stream.ofAll(Position.ADJACENT_AND_DIAGONAL.values())
                    .map(p -> sourcePos.add(p))
                    .append(sourcePos)
                    .flatMap(pos -> {
                        return state.getTileFeatures2(pos, Structure.class).map(t -> {
                            FeaturePointer fp = new FeaturePointer(pos, t._1);
                            return new Tuple2<>(fp, t._2);
                        });
                    });
        }
    }


    @PhaseMessageHandler
    @Override
    public StepResult handlePass(GameState state, PassMessage msg) {
        if (!state.getPlayerActions().isPassAllowed()) {
            throw new IllegalStateException("Pass is not allowed");
        }

        state = clearActions(state);
        return enter(state);
    }

    @PhaseMessageHandler
    public StepResult handleCommit(GameState state, CommitMessage msg) {
        RussianPromosTrapCapability russianPromos = state.getCapabilities().get(RussianPromosTrapCapability.class);
        if (russianPromos != null) {
            state = russianPromos.trapFollowers(state);
        }

        state = clearActions(state);
        return enter(state);
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Player player = state.getActivePlayer();
        Meeple m = player.getMeepleFromSupply(state, msg.getMeepleId());
        if (!(m instanceof Wagon)) {
            throw new IllegalArgumentException("Invalid follower");
        }
        //TODO validate against players actions

        state = (new DeployMeeple(m, fp)).apply(state);

        Queue<Tuple2<Wagon, FeaturePointer>> model = state.getCapabilityModel(WagonCapability.class);
        if (model.find(t -> t._1.getPlayer().equals(player)).isEmpty()) {
            // player has no other wagon to deploy (can happen only with multiple wagons setup)
            return promote(state.setPlayerActions(new ActionsState(player, new ConfirmAction(), false)));
        } else {
            state = clearActions(state);
        }

        return enter(state);
    }
}
