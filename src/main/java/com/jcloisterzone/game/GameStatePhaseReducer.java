package com.jcloisterzone.game;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.jcloisterzone.game.phase.AbbeyPhase;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.BazaarPhase;
import com.jcloisterzone.game.phase.CastlePhase;
import com.jcloisterzone.game.phase.ChangeFerriesPhase;
import com.jcloisterzone.game.phase.CleanUpTurnPartPhase;
import com.jcloisterzone.game.phase.CleanUpTurnPhase;
import com.jcloisterzone.game.phase.CocCountPhase;
import com.jcloisterzone.game.phase.CocFinalScoringPhase;
import com.jcloisterzone.game.phase.CocFollowerPhase;
import com.jcloisterzone.game.phase.CocScoringPhase;
import com.jcloisterzone.game.phase.CommitAbbeyPassPhase;
import com.jcloisterzone.game.phase.CommitActionPhase;
import com.jcloisterzone.game.phase.CornCirclePhase;
import com.jcloisterzone.game.phase.DragonMovePhase;
import com.jcloisterzone.game.phase.DragonPhase;
import com.jcloisterzone.game.phase.EscapePhase;
import com.jcloisterzone.game.phase.FairyPhase;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.GoldPiecePhase;
import com.jcloisterzone.game.phase.MageAndWitchPhase;
import com.jcloisterzone.game.phase.PhantomPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.PhaseMessageHandler;
import com.jcloisterzone.game.phase.PlaceFerryPhase;
import com.jcloisterzone.game.phase.RequiredCapability;
import com.jcloisterzone.game.phase.ScoringPhase;
import com.jcloisterzone.game.phase.ShepherdPhase;
import com.jcloisterzone.game.phase.StepResult;
import com.jcloisterzone.game.phase.TilePhase;
import com.jcloisterzone.game.phase.TowerCapturePhase;
import com.jcloisterzone.game.phase.WagonPhase;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.Function2;

/**
 * Derives new game state by applying WsMessage.
 */
public class GameStatePhaseReducer implements Function2<GameState, WsInGameMessage, GameState> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final ClassToInstanceMap<Phase> phases = MutableClassToInstanceMap.create();
    private final Phase firstPhase;
    private final RandomGenerator random;

    public GameStatePhaseReducer(GameSetup setup, long initialSeed) {
        random = new RandomGenerator(initialSeed);

        Phase over, last, next = null;
        //if there isn't assignment - phase is out of standard flow
        over = addPhase(setup, next, GameOverPhase.class);
               addPhase(setup, over, CocFinalScoringPhase.class);

        next = last = addPhase(setup, next, CleanUpTurnPhase.class);
        next = addPhase(setup, next, BazaarPhase.class);
        next = addPhase(setup, next, EscapePhase.class);
        next = addPhase(setup, next, CleanUpTurnPartPhase.class);
        next = addPhase(setup, next, CornCirclePhase.class);

        if (setup.getBooleanValue(Rule.DRAGON_MOVE_AFTER_SCORING)) {
            addPhase(setup, next, DragonMovePhase.class);
            next = addPhase(setup, next, DragonPhase.class);
        }

               addPhase(setup, next, CocCountPhase.class);
        next = addPhase(setup, next, CocFollowerPhase.class);
        next = addPhase(setup, next, WagonPhase.class);
        next = addPhase(setup, next, ScoringPhase.class);
        next = addPhase(setup, next, CocScoringPhase.class);
        next = addPhase(setup, next, CommitActionPhase.class);
        next = addPhase(setup, next, CastlePhase.class);

        if (!setup.getBooleanValue(Rule.DRAGON_MOVE_AFTER_SCORING)) {
               addPhase(setup, next, DragonMovePhase.class);
               next = addPhase(setup, next, DragonPhase.class);
        }

        next = addPhase(setup, next, ShepherdPhase.class);
        next = addPhase(setup, next, ChangeFerriesPhase.class);
        next = addPhase(setup, next, PlaceFerryPhase.class);
        next = addPhase(setup, next, PhantomPhase.class);
               addPhase(setup, next, TowerCapturePhase.class);
        next = addPhase(setup, next, ActionPhase.class);
        next = addPhase(setup, next, MageAndWitchPhase.class);
        next = addPhase(setup, next, GoldPiecePhase.class);
        next = addPhase(setup, next, TilePhase.class);
        // if abbey is passed, commit commit action phase follows to change salt by following Commit message
        next = addPhase(setup, next, CommitAbbeyPassPhase.class);
        next = addPhase(setup, next, AbbeyPhase.class);
        next = addPhase(setup, next, FairyPhase.class);
        last.setDefaultNext(next); //after last phase, the first is default

        firstPhase = next;
    }

    private Phase addPhase(GameSetup setup, Phase next, Class<? extends Phase> phaseClass) {
        RequiredCapability req = phaseClass.getAnnotation(RequiredCapability.class);

        if (req != null && !setup.getCapabilities().contains(req.value())) {
            return next;
        }

        Phase phase;
        try {
            phase = phaseClass.getConstructor(RandomGenerator.class).newInstance(random);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        phases.put(phaseClass, phase);
        if (next != null) {
            phase.setDefaultNext(next);
        }
        return phase;
    }

    private StepResult applyMessageOnPhase(Phase phase, GameState state, WsInGameMessage message) {
        for (Method m : phase.getClass().getMethods()) {
            if (m.getAnnotation(PhaseMessageHandler.class) == null) {
                continue;
            }
            Class<?> params[] = m.getParameterTypes();
            assert params.length == 2;

            Class<?> acceptedMessageClass = params[1];
            // check exact class instead of isInstance -
            // eg. DeployFlierMessage extends DeployMeepleMessage but can have separate handlers
            if (!acceptedMessageClass.equals(message.getClass())) {
                continue;
            }
            try {
                assert m.getReturnType().equals(StepResult.class) : String.format("Bad return type %s.%s()", phase.getClass().getSimpleName(), m.getName());
                return (StepResult) m.invoke(phase, state, message);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause() == null ? e : e.getCause());
            }
        }

        MessageParser parser = new MessageParser();
        logger.warn("Unhandled message:\n\t" +  parser.toJson(message));
        throw new MessageNotHandledException(String.format("Message %s hasn't been handled by %s phase.", message.getClass().getSimpleName(), phase));
    }

    public GameState applyStepResult(StepResult stepResult) {
        GameState state = stepResult.getState();
        while (stepResult.getNext() != null) {
            stepResult = getPhase(stepResult.getNext()).enter(state);
            state = stepResult.getState();
        }
        return state;
    }

    @Override
    public GameState apply(GameState state, WsInGameMessage message) {
        Phase phase = getPhase(state.getPhase());
        StepResult stepResult = applyMessageOnPhase(phase, state, message);
        return applyStepResult(stepResult);
    }

    public Phase getFirstPhase() {
        return firstPhase;
    }

    public Phase getPhase(Class<? extends Phase> cls) {
        return phases.get(cls);
    }

    public RandomGenerator getRandom() {
        return random;
    }


}
