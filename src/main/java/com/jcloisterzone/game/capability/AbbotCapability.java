package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.CloisterLike;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Garden;
import com.jcloisterzone.figure.Abbot;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

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

            Feature feature = state.getFeature(t._2);
            if (!(feature instanceof CloisterLike)) {
                // eg catched by Vodyanoy
                continue;
            }

            CloisterLike cloister = (CloisterLike) feature;
            if (cloister.isCompleted(state)) {
                /* https://wikicarpedia.com/index.php/The_Abbot#cite_note-14
                You can only score the abbot placed on a monastery or a garden before the feature is completely
                surrounded by tiles, that is, when the feature is incomplete. In this case, you always score the abbot
                (on the feature), never the feature itself. (11/2020)
                */
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
