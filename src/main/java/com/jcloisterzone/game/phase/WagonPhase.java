package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;

@RequiredCapability(WagonCapability.class)
public class WagonPhase extends Phase {

    public WagonPhase(GameController gc) {
        super(gc);
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
            Completable feature = (Completable) state.getFeature(item._2);
            GameState _state = state;
            Set<FeaturePointer> options = feature.getNeighboring()
                .filter(fp -> {
                    Completable nei = (Completable) _state.getFeature(fp);
                    return !nei.isCompleted(_state) && nei.getFollowers(_state).isEmpty();
                });

            if (!options.isEmpty()) {
                PlayerAction<?> action = new MeepleAction(Wagon.class, options);
                state = state.setPlayerActions(
                    new ActionsState(wagon.getPlayer(), action, true)
                );
                return promote(state);
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
        game.markUndo();
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
