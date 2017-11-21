package com.jcloisterzone.ai;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.GameStatePhaseReducer;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

public abstract class RankingAiPlayer implements AiPlayer {

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
    public Vector<WsInGameMessage> getPossibleActions(GameState state) {
        ActionsState as = state.getPlayerActions();
        PlacedTile lastPlaced = state.getLastPlaced();

        // DIRTY HACK for portal, try each feature only once
        // TODO solve it better. eg using solution from 3.x, this is still quite slow
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Vector<WsInGameMessage> messages = as.getActions().flatMap(action -> {
            Set options = action.getOptions();
            if (action instanceof MeepleAction && lastPlaced != null && lastPlaced.getTile().getTrigger() == TileTrigger.PORTAL) {
                Set<FeaturePointer> _options = options;
                options = _options.groupBy(fp -> state.getFeature(fp)).values().map(s -> s.getOrNull()).toSet();
            }
            return options.map(o -> AiPlayer.Helpers.createMessage(action, o)).toVector();
        });

        if (as.isPassAllowed()) {
            messages = messages.append(new PassMessage());
        }

        return messages;
    }

    @Override
    public WsInGameMessage apply(GameState state) {
        if (messages.isEmpty()) {
            Queue<Tuple2<GameState, Vector<WsInGameMessage>>> queue = Queue.of(new Tuple2<>(state, Vector.empty()));
            Double bestSoFar = Double.NEGATIVE_INFINITY;

            while (!queue.isEmpty()) {
                Tuple2<Tuple2<GameState, Vector<WsInGameMessage>>, Queue<Tuple2<GameState, Vector<WsInGameMessage>>>> t = queue.dequeue();
                queue = t._2;
                Tuple2<GameState, Vector<WsInGameMessage>> item = t._1;
                GameState itemState = item._1;
                for (WsInGameMessage msg : getPossibleActions(itemState)) {
                    Vector<WsInGameMessage> chain = item._2.append(msg);
                    GameState newState = phaseReducer.apply(itemState, msg);
                    if (newState.getActivePlayer() == me) {
                        queue = queue.enqueue(new Tuple2<>(newState, chain));
                    } else {
                        Double ranking = stateRanking.apply(newState);

//                        String chainStr = chain.map(_msg -> _msg.getClass().getSimpleName()).toJavaStream().collect(Collectors.joining(", "));
//                        System.err.println(String.format(">>> %f\n%s", ranking, chainStr));

                        if (ranking > bestSoFar) {
                            bestSoFar = ranking;
                            messages = chain;
                        }
                    }
                }
            }

//            System.err.println("====>");
//            String chainStr = messages.map(_msg -> _msg.getClass().getSimpleName()).toJavaStream().collect(Collectors.joining(", "));
//            System.err.println(String.format(">>> %f\n%s", bestSoFar, chainStr));
        }

        WsInGameMessage msg = messages.get();
        messages = messages.drop(1);
        return msg;
    }

}
