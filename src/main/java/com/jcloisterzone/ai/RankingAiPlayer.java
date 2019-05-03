package com.jcloisterzone.ai;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.GameStatePhaseReducer;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.PlaceTileMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsSaltMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Vector;

public abstract class RankingAiPlayer implements AiPlayer {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private GameStateRanking stateRanking;
    private GameStatePhaseReducer phaseReducer;

    private Player me;
    private Vector<WsInGameMessage> messages = Vector.empty();

    protected abstract GameStateRanking createStateRanking(Player me);

    @Override
    public void onGameStart(GameSetup setup, Player me) {
        this.me = me;
        phaseReducer = new GameStatePhaseReducer(setup, 0);
        stateRanking = createStateRanking(me);
    }

    @Override
    public WsInGameMessage apply(GameState state) {
        if (messages.isEmpty()) {
            Double bestSoFar = Double.NEGATIVE_INFINITY;
            Queue<Tuple2<GameState, Vector<WsInGameMessage>>> queue = Queue.of(new Tuple2<>(state, Vector.empty()));

            while (!queue.isEmpty()) {
                Tuple2<Tuple2<GameState, Vector<WsInGameMessage>>, Queue<Tuple2<GameState, Vector<WsInGameMessage>>>> t = queue.dequeue();
                queue = t._2;
                Tuple2<GameState, Vector<WsInGameMessage>> item = t._1;
                GameState itemState = item._1;

                for (WsInGameMessage msg : getPossibleActions(itemState)) {
                    Vector<WsInGameMessage> chain = item._2.append(msg);
                    GameState newState = phaseReducer.apply(itemState, msg);
                    boolean end = newState.getActivePlayer() != me || newState.getTurnPlayer() != state.getTurnPlayer() || msg instanceof WsSaltMessage;

                    if (!end && msg instanceof PlaceTileMessage &&
                        newState.getLastPlaced().getTile().hasModifier(PortalCapability.MAGIC_PORTAL)) {
                        // hack to avoid bad performance on Portal tile
                        // rank just placement then rang meeple placement separately
                        // still not perfect because it can miss good on tile meeple placement
                        end = true;
                    }

                    if (end) {
                        Double ranking = stateRanking.apply(newState);

//                      String chainStr = chain.map(_msg -> _msg.getClass().getSimpleName()).toJavaStream().collect(Collectors.joining(", "));
//                      System.err.println(String.format(">>> %f\n%s", ranking, chainStr));

                        if (ranking > bestSoFar) {
                            bestSoFar = ranking;
                            messages = chain;
                        }
                    } else {
                        queue = queue.enqueue(new Tuple2<>(newState, chain));
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                String chainStr = messages.map(_msg -> _msg.getClass().getSimpleName()).toJavaStream().collect(Collectors.joining(", "));
                logger.debug(String.format("Best ranking %s, %s", bestSoFar, chainStr));
            }
        }

        WsInGameMessage msg = messages.get();
        messages = messages.drop(1);

        return msg;
    }

}
