package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.action.NeutralFigureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.RemovMageOrWithAction;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.capability.MageAndWitchCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

@RequiredCapability(MageAndWitchCapability.class)
public class MageAndWitchPhase extends Phase {

    public MageAndWitchPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    public StepResult enter(GameState state) {
        TileDefinition tile = state.getLastPlaced().getTile();
        NeutralFiguresState ns = state.getNeutralFigures();
        Completable mageFeature = (Completable) ns.getMage().getFeature(state);
        Completable witchFeature = (Completable) ns.getWitch().getFeature(state);

        if (tile.getTrigger() != TileTrigger.MAGE && mageFeature != witchFeature) {
            return next(state);
        }

        // tile has trigger or mage and witch feature has been joined together

        GameState _state = state;
        Stream<Completable> targetFeatures = state.getFeatures(Completable.class)
            .filter(f -> f != mageFeature && f != witchFeature)
            .filter(f -> f instanceof Road || f instanceof City)
            .filter(f -> f.isOpen(_state));

        if (targetFeatures.isEmpty()) {
            if (mageFeature != null && witchFeature != null) {
            /*  If it is not possible to place or move the mage or witch figure
                (because there are no unfinished cities or roads), the player must
                remove either the mage or witch from the board, if at least one is on a tile. */
                RemovMageOrWithAction action = new RemovMageOrWithAction(HashSet.of(ns.getMage(), ns.getWitch()));
                return promote(state.setPlayerActions(
                    new ActionsState(state.getTurnPlayer(), action, false)
                ));
            }

            if (mageFeature != null) {
                state = (new MoveNeutralFigure<>(ns.getMage(), null)).apply(state);
            }
            if (witchFeature != null) {
                state = (new MoveNeutralFigure<>(ns.getWitch(), null)).apply(state);
            }
            return next(state);
        }

        Set<FeaturePointer> options = targetFeatures.flatMap(Completable::getPlaces).toSet();
        Vector<PlayerAction<?>> actions = Vector.of(
            new NeutralFigureAction(ns.getMage(), options),
            new NeutralFigureAction(ns.getWitch(), options)
        );
        return promote(state.setPlayerActions(
            new ActionsState(state.getTurnPlayer(), actions, false)
        ));
    }

    @PhaseMessageHandler
    public StepResult handleMoveNeutralFigure(GameState state, MoveNeutralFigureMessage msg) {
        FeaturePointer ptr = (FeaturePointer) msg.getTo();
        @SuppressWarnings("unchecked")
        NeutralFigure<FeaturePointer> fig = (NeutralFigure<FeaturePointer>) state.getNeutralFigures().getById(msg.getFigureId());

        if (fig instanceof Mage || fig instanceof Witch) {
            state = (new MoveNeutralFigure<FeaturePointer>(fig, ptr, state.getActivePlayer())).apply(state);
            state = clearActions(state);
            return next(state);
        } else {
            throw new IllegalArgumentException("Illegal neutral figure move");
        }
    }
}
