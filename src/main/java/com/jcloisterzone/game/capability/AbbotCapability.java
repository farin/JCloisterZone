package com.jcloisterzone.game.capability;

import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Abbot;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;

public class AbbotCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        ActionsState actions = state.getPlayerActions();
        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple meeple = t._1;
            if (!(meeple instanceof Abbot) || meeple.getPlayer() != actions.getPlayer()) {
                continue;
            }
            actions = actions.appendAction(new ReturnMeepleAction(
                    HashSet.of(new MeeplePointer(t._2, meeple.getId())),
                    ReturnMeepleSource.ABBOT_RETURN));
            state = state.setPlayerActions(actions);
        }
        return state;
    }
}
