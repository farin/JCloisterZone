package com.jcloisterzone.game.capability;

import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.collection.Set;

public class RobbersSonCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        if (state.getFlags().contains(Flag.NO_PHANTOM)) {
            return state;
        }

        PlacedTile lastTile = state.getLastPlaced();
        Set<MeeplePointer> options = state.getTileFeatures2(lastTile.getPosition(), Scoreable.class)
        .filter(t -> {
            if (t._2 instanceof Road) {
                Road part = (Road) lastTile.getInitialFeaturePartOf(t._1.getLocation())._2;
                return part.hasModifier(state, Road.ROBBERS_SON);
            } else {
                return false;
            }
        })
        .flatMap(featureTuple -> {
            Road roadWithReturn = (Road) featureTuple._2;
            return roadWithReturn.getFollowers2(state).map(MeeplePointer::new);
        })
        .toSet();

        if (options.isEmpty()) {
            return state;
        }

        return state.appendAction(new ReturnMeepleAction(options, ReturnMeepleSource.ROBBERS_SON));
    }
}
