package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;

@RequiredCapability(WagonCapability.class)
public class WagonPhase extends Phase {

    public WagonPhase(RandomGenerator random) {
        super(random);
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
                Set<FeaturePointer> options = ((Completable)feature).getNeighboring()
                    .filter(fp -> {
                        Feature f = _state.getFeature(fp);
                        if (f instanceof Castle) {
                            Castle castle = (Castle) f;
                            return !castle.isOccupied(_state);
                        }
                        if (f instanceof Completable) {
                            Completable nei = (Completable) f;
                            return !nei.isCompleted(_state) && !nei.isOccupied(_state);
                        }
                        return false; // eg f == null
                    });

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
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Meeple m = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());
        if (!(m instanceof Wagon)) {
            throw new IllegalArgumentException("Invalid follower");
        }
        //TODO validate against players actions

        state = (new DeployMeeple(m, fp)).apply(state);
        state = clearActions(state);
        return enter(state);
    }
}
