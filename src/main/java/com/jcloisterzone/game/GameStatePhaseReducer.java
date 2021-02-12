package com.jcloisterzone.game;

import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.phase.*;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.MessageParser;
import com.jcloisterzone.io.message.CommitMessage;
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
        AbbeyPhase abbeyPhase = null;
        AbbeyEndGamePhase abbeyEndGamePhase = null;

        endChain =                  new GameOverPhase(random, null);
        if (setup.contains(CountCapability.class)) endChain = new CocFinalScoringPhase(random, endChain);
        if (setup.contains(AbbeyCapability.class)) endChain = abbeyEndGamePhase = new AbbeyEndGamePhase(random, endChain);

        next = cleanUpTurnPhase = new CleanUpTurnPhase(random, null);
        if (setup.contains(BazaarCapability.class)) next = new BazaarPhase(random, next);
        if (setup.getBooleanRule(Rule.ESCAPE)) next = new EscapePhase(random, next);
        next = cleanUpTurnPartPhase = new CleanUpTurnPartPhase(random, next);
        if (setup.contains(CornCircleCapability.class)) new CornCirclePhase(random, next);

        if (setup.contains(DragonCapability.class) && "after-scoring".equals(setup.getStringRule(Rule.DRAGON_MOVEMENT))) {
            next = new DragonPhase(random, next);
        }
        if (setup.contains(CountCapability.class)) next = new CocFollowerPhase(random, next);
        if (setup.contains(WagonCapability.class)) next = new WagonPhase(random, next);
        next = new ScoringPhase(random, next);
        if (setup.contains(CountCapability.class)) next = new CocScoringPhase(random, next);
        next = new CommitActionPhase(random, next);
        if (setup.contains(CastleCapability.class)) next = new CastlePhase(random, next);
        if (setup.contains(DragonCapability.class) && !"after-scoring".equals(setup.getStringRule(Rule.DRAGON_MOVEMENT))) {
            next = new DragonPhase(random, next);
        }
        if (setup.contains(SheepCapability.class)) next = new ShepherdPhase(random, next);
        if (setup.contains(FerriesCapability.class)) {
            next = new ChangeFerriesPhase(random, next);
            next = new PlaceFerryPhase(random, next);
        }
        if (setup.contains(PhantomCapability.class)) next = new PhantomPhase(random, next);
        if (setup.contains(RussianPromosTrapCapability.class)) next = new RussianPromosTrapPhase(random, next);
        next = actionPhase = new ActionPhase(random, next);
        if (setup.contains(MageAndWitchCapability.class)) next =  new MageAndWitchPhase(random, next);
        if (setup.contains(GoldminesCapability.class)) next =  new GoldPiecePhase(random, next);
        next = tilePhase = new TilePhase(random, next);
        if (setup.contains(AbbeyCapability.class)) {
            // if abbey is passed, commit commit action phase follows to change salt by following Commit message
            next = new CommitAbbeyPassPhase(random, next);
            next = abbeyPhase = new AbbeyPhase(random, next);
        }
        if (setup.contains(FairyCapability.class)) next = new FairyPhase(random, next);

        cleanUpTurnPhase.setDefaultNext(next); //after last phase, the first is default
        cleanUpTurnPhase.setAbbeyEndGamePhase(abbeyEndGamePhase);
        cleanUpTurnPhase.setEndPhase(endChain);
        cleanUpTurnPartPhase.setSecondPartStartPhase(abbeyPhase != null ? abbeyPhase : tilePhase);
        if (abbeyEndGamePhase != null) abbeyEndGamePhase.setActionPhase(actionPhase);
        if (abbeyPhase != null) {
            abbeyPhase.setTilePhase(tilePhase);
            abbeyPhase.setActionPhase(actionPhase);
        }
        tilePhase.setEndPhase(endChain);
        tilePhase.setCleanUpTurnPhase(cleanUpTurnPhase);
        firstPhase = next;
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
                StepResult res = (StepResult) m.invoke(phase, state, message);
                boolean commited = message instanceof CommitMessage;
                if (res.getState().isCommited() != commited) {
                    res = new StepResult(res.getState().setCommited(commited), res.getNext());
                }
                return res;
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
