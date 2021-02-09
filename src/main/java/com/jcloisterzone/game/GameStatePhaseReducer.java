package com.jcloisterzone.game;

import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.phase.*;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.MessageParser;
import com.jcloisterzone.io.message.Message;
import io.vavr.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Derives new game state by applying Message.
 */
public class GameStatePhaseReducer implements Function2<GameState, Message, GameState> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    // private final Map<Class<? extends Phase>, Phase> phases = new HashMap<>();
    private final Phase firstPhase;
    private final RandomGenerator random;

    public GameStatePhaseReducer(GameSetup setup, long initialSeed) {
        random = new RandomGenerator(initialSeed);

        Phase endChain, next;

        CleanUpTurnPhase cleanUpTurnPhase;
        CleanUpTurnPartPhase cleanUpTurnPartPhase;
        TilePhase tilePhase;
        ActionPhase actionPhase;
        AbbeyPhase abbeyPhase;
        AbbeyEndGamePhase abbeyEndGamePhase;

        endChain =                  new GameOverPhase(random, null);
        endChain = ifEnabled(setup, new CocFinalScoringPhase(random, endChain));
        endChain = ifEnabled(setup, abbeyEndGamePhase = new AbbeyEndGamePhase(random, endChain));

        next = cleanUpTurnPhase = new CleanUpTurnPhase(random, null);
        next = ifEnabled(setup, new BazaarPhase(random, next));

        if (setup.getBooleanRule(Rule.ESCAPE)) {
            next = ifEnabled(setup, new EscapePhase(random, next));
        }
        next = cleanUpTurnPartPhase = new CleanUpTurnPartPhase(random, next);
        next = ifEnabled(setup, new CornCirclePhase(random, next));

        if ("after-scoring".equals(setup.getStringRule(Rule.DRAGON_MOVEMENT))) {
            next = ifEnabled(setup, new DragonPhase(random, next));
        }

               ifEnabled(setup, new CocCountPhase(random, next));
        next = ifEnabled(setup, new CocFollowerPhase(random, next));
        next = ifEnabled(setup, new WagonPhase(random, next));
        next =                  new ScoringPhase(random, next);
        next = ifEnabled(setup, new CocScoringPhase(random, next));
        next =                  new CommitActionPhase(random, next);
        next = ifEnabled(setup, new CastlePhase(random, next));

        if (!"after-scoring".equals(setup.getStringRule(Rule.DRAGON_MOVEMENT))) {
               next = ifEnabled(setup, new DragonPhase(random, next));
        }

        next = ifEnabled(setup, new ShepherdPhase(random, next));
        next = ifEnabled(setup, new ChangeFerriesPhase(random, next));
        next = ifEnabled(setup, new PlaceFerryPhase(random, next));
        next = ifEnabled(setup, new PhantomPhase(random, next));
        // next = addPhase(setup, new RussianPromosTrapPhase(random, next));
        next = actionPhase =    new ActionPhase(random, next);
        next = ifEnabled(setup, new MageAndWitchPhase(random, next));
        next = ifEnabled(setup, new GoldPiecePhase(random, next));
        next = ifEnabled(setup, new RussianPromosTrapPhase(random, next));
        next = tilePhase =      new TilePhase(random, next);
        // if abbey is passed, commit commit action phase follows to change salt by following Commit message
        next = ifEnabled(setup, new CommitAbbeyPassPhase(random, next));
        next = ifEnabled(setup, abbeyPhase = new AbbeyPhase(random, next));
        next = ifEnabled(setup, new FairyPhase(random, next));

        cleanUpTurnPhase.setDefaultNext(next); //after last phase, the first is default
        cleanUpTurnPhase.setAbbeyEndGamePhase(abbeyEndGamePhase);
        cleanUpTurnPhase.setEndPhase(endChain);
        cleanUpTurnPartPhase.setSecondPartStartPhase(setup.getCapabilities().contains(AbbeyCapability.class) ? abbeyPhase : tilePhase);
        abbeyEndGamePhase.setActionPhase(actionPhase);
        abbeyPhase.setTilePhase(tilePhase);
        abbeyPhase.setActionPhase(actionPhase);
        tilePhase.setEndPhase(endChain);
        tilePhase.setCleanUpTurnPhase(cleanUpTurnPhase);
        firstPhase = next;
    }

    private Phase ifEnabled(GameSetup setup, Phase phase) {
        RequiredCapability req = phase.getClass().getAnnotation(RequiredCapability.class);

        if (req != null && !setup.getCapabilities().contains(req.value())) {
            return phase.getDefaultNext();
        }
        return phase;
    }

    private StepResult applyMessageOnPhase(Phase phase, GameState state, Message message) {
        for (Method m : phase.getClass().getMethods()) {
            if (m.getAnnotation(PhaseMessageHandler.class) == null) {
                continue;
            }
            Class<?>[] params = m.getParameterTypes();
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
        throw new MessageNotHandledException(String.format("MessageCommand %s hasn't been handled by %s phase.", message.getClass().getSimpleName(), phase));
    }

    public GameState applyStepResult(StepResult stepResult) {
        GameState state = stepResult.getState();
        while (stepResult.getNext() != null) {
            stepResult = stepResult.getNext().enter(state);
            state = stepResult.getState();
        }
        return state;
    }

    @Override
    public GameState apply(GameState state, Message message) {
        Phase phase = state.getPhase();
        StepResult stepResult = applyMessageOnPhase(phase, state, message);
        return applyStepResult(stepResult);
    }

    public Phase getFirstPhase() {
        return firstPhase;
    }

    public RandomGenerator getRandom() {
        return random;
    }


}
