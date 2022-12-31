package com.jcloisterzone.game;

import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.phase.*;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.MessageParser;
import com.jcloisterzone.io.message.CommitMessage;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.PassMessage;
import com.jcloisterzone.random.RandomGenerator;
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

    private final Phase firstPhase;
    private final RandomGenerator randomGenerator;

    public GameStatePhaseReducer(GameSetup setup, double initialRandom) {
        randomGenerator = new RandomGenerator(initialRandom);
        Phase endChain, next;

        CleanUpTurnPhase cleanUpTurnPhase;
        CleanUpTurnPartPhase cleanUpTurnPartPhase;
        TilePhase tilePhase;
        ActionPhase actionPhase;
        AbbeyPhase abbeyPhase = null;
        AbbeyEndGamePhase abbeyEndGamePhase = null;

        endChain = new GameOverPhase(randomGenerator, null);
        if (setup.contains(CountCapability.class)) endChain = new CocFinalScoringPhase(randomGenerator, endChain);
        if (setup.contains(AbbeyCapability.class)) endChain = abbeyEndGamePhase = new AbbeyEndGamePhase(randomGenerator, endChain);

        next = cleanUpTurnPhase = new CleanUpTurnPhase(randomGenerator, null);
        if (setup.contains(BazaarCapability.class)) next = new BazaarPhase(randomGenerator, next);
        if (setup.getBooleanRule(Rule.ESCAPE)) next = new EscapePhase(randomGenerator, next);
        next = cleanUpTurnPartPhase = new CleanUpTurnPartPhase(randomGenerator, next);
        if (setup.contains(CornCircleCapability.class)) next = new CornCirclePhase(randomGenerator, next);

        if (setup.contains(DragonCapability.class) && "after-scoring".equals(setup.getStringRule(Rule.DRAGON_MOVEMENT))) {
            next = new DragonPhase(randomGenerator, next);
        }
        if (setup.contains(CountCapability.class)) next = new CocFollowerPhase(randomGenerator, next);
        if (setup.contains(BigTopCapability.class)) next = new BigTopPhase(randomGenerator, next);
        if (setup.contains(WagonCapability.class)) next = new WagonPhase(randomGenerator, next);
        next = new ScoringPhase(randomGenerator, next);
        if (setup.contains(CountCapability.class)) next = new CocScoringPhase(randomGenerator, next);
        next = new CommitActionPhase(randomGenerator, next);
        if (setup.contains(CastleCapability.class)) next = new CastlePhase(randomGenerator, next);
        if (setup.contains(DragonCapability.class) && !"after-scoring".equals(setup.getStringRule(Rule.DRAGON_MOVEMENT))) {
            next = new DragonPhase(randomGenerator, next);
        }
        if (setup.contains(SheepCapability.class)) next = new ShepherdPhase(randomGenerator, next);
        if (setup.contains(FerriesCapability.class)) {
            next = new ChangeFerriesPhase(randomGenerator, next);
            next = new PlaceFerryPhase(randomGenerator, next);
        }
        if (setup.contains(RussianPromosTrapCapability.class)) next = new RussianPromosTrapPhase(randomGenerator, next);
        if (setup.contains(TunnelCapability.class)) next = new TunnelPhase(randomGenerator, next);
        if (setup.contains(PhantomCapability.class)) next = new PhantomPhase(randomGenerator, next);
        if (setup.contains(RussianPromosTrapCapability.class)) next = new RussianPromosTrapPhase(randomGenerator, next);
        next = actionPhase = new ActionPhase(randomGenerator, next);
        if (setup.contains(MageAndWitchCapability.class)) next =  new MageAndWitchPhase(randomGenerator, next);
        if (setup.contains(GoldminesCapability.class)) next =  new GoldPiecePhase(randomGenerator, next);
        next = tilePhase = new TilePhase(randomGenerator, next);
        if (setup.contains(AbbeyCapability.class)) {
            // if abbey is passed, commit action phase follows to change salt by following Commit message
            next = new CommitAbbeyPassPhase(randomGenerator, next);
            next = abbeyPhase = new AbbeyPhase(randomGenerator, next);
        }
        if (setup.contains(FairyCapability.class)) next = new FairyPhase(randomGenerator, next);

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

    public RandomGenerator getRandomGanerator() {
        return randomGenerator;
    }
}
