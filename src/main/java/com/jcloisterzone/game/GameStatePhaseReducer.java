package com.jcloisterzone.game;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.phase.AbbeyPhase;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.BazaarPhase;
import com.jcloisterzone.game.phase.CastlePhase;
import com.jcloisterzone.game.phase.CleanUpTurnPartPhase;
import com.jcloisterzone.game.phase.CleanUpTurnPhase;
import com.jcloisterzone.game.phase.CocCountPhase;
import com.jcloisterzone.game.phase.CocFollowerPhase;
import com.jcloisterzone.game.phase.CocPreScorePhase;
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
import com.jcloisterzone.game.phase.RequiredCapability;
import com.jcloisterzone.game.phase.ScorePhase;
import com.jcloisterzone.game.phase.StepResult;
import com.jcloisterzone.game.phase.TilePhase;
import com.jcloisterzone.game.phase.TowerCapturePhase;
import com.jcloisterzone.game.phase.WagonPhase;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.Function2;

/**
 * Derives new game state by applying WsMessage.
 */
public class GameStatePhaseReducer implements Function2<GameState, WsInGameMessage, GameState> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final ClassToInstanceMap<Phase> phases = MutableClassToInstanceMap.create();
    private final Phase firstPhase;

    private final Random random;

    public GameStatePhaseReducer(Config config, GameSetup setup, long initialSeed) {
        random = new Random(initialSeed);

        Phase last, next = null;
        //if there isn't assignment - phase is out of standard flow
               addPhase(config, setup, next, GameOverPhase.class);
        next = last = addPhase(config, setup, next, CleanUpTurnPhase.class);
        next = addPhase(config, setup, next, BazaarPhase.class);
        next = addPhase(config, setup, next, EscapePhase.class);
        next = addPhase(config, setup, next, CleanUpTurnPartPhase.class);
        next = addPhase(config, setup, next, CornCirclePhase.class);

        if (setup.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
            addPhase(config, setup, next, DragonMovePhase.class);
            next = addPhase(config, setup, next, DragonPhase.class);
        }

               addPhase(config, setup, next, CocCountPhase.class);
        next = addPhase(config, setup, next, CocFollowerPhase.class);
        next = addPhase(config, setup, next, WagonPhase.class);
        next = addPhase(config, setup, next, ScorePhase.class);
        next = addPhase(config, setup, next, CocPreScorePhase.class);
        next = addPhase(config, setup, next, CommitActionPhase.class);
        next = addPhase(config, setup, next, CastlePhase.class);

        if (!setup.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
               addPhase(config, setup, next, DragonMovePhase.class);
               next = addPhase(config, setup, next, DragonPhase.class);
        }

        next = addPhase(config, setup, next, PhantomPhase.class);
               addPhase(config, setup, next, TowerCapturePhase.class);
        next = addPhase(config, setup, next, ActionPhase.class);
        next = addPhase(config, setup, next, MageAndWitchPhase.class);
        next = addPhase(config, setup, next, GoldPiecePhase.class);
        next = addPhase(config, setup, next, TilePhase.class);
        next = addPhase(config, setup, next, AbbeyPhase.class);
        next = addPhase(config, setup, next, FairyPhase.class);
        last.setDefaultNext(next); //after last phase, the first is default

        firstPhase = next;
    }

    public void updateRandomSeed(long seed) {
        random.setSeed(seed);
    }

    private Phase addPhase(Config config, GameSetup setup, Phase next, Class<? extends Phase> phaseClass) {
        RequiredCapability req = phaseClass.getAnnotation(RequiredCapability.class);

        if (req != null && !setup.getCapabilities().contains(req.value())) {
            return next;
        }

        Phase phase;
        try {
            phase = phaseClass.getConstructor(Config.class, Random.class).newInstance(config, random);
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

            Class<?> acceptedMessageClass = (Class<?>) params[1];
            if (!acceptedMessageClass.isInstance(message)) {
                continue;
            }
            try {
                return (StepResult) m.invoke(phase, state, message);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause() == null ? e : e.getCause());
            }
        }
        throw new IllegalArgumentException(String.format("Message %s hasn't been handled by %s phase.", message.getClass().getSimpleName(), phase));
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
}
