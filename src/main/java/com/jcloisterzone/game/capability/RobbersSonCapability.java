package com.jcloisterzone.game.capability;

import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.modifier.BooleanModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.collection.Set;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class RobbersSonCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    public static final BooleanModifier ROBBERSSON = new BooleanModifier("robbers-son");

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Road && attributeBoolValue(xml, "robbersson")) {
            feature = ((Road)feature).putModifier(ROBBERSSON, true);
        }
        return feature;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        if (state.getFlags().contains(Flag.PRINCESS_USED) || state.getFlags().contains(Flag.ROBBERSSON_USED)) {
            return state;
        }

        PlacedTile lastTile = state.getLastPlaced();
        Set<MeeplePointer> options = state.getTileFeatures2(lastTile.getPosition(), Scoreable.class)
        .filter(t -> {
            if (t._2 instanceof Road) {
                Road part = (Road) lastTile.getInitialFeaturePartOf(t._1);
                return part.hasModifier(RobbersSonCapability.ROBBERSSON);
            } else {
                return false;
            }
        })
        .flatMap(featureTuple -> {
            Road roadWithRobbersSon = (Road) featureTuple._2;
            return roadWithRobbersSon.getFollowers2(state).map(MeeplePointer::new);
        })
        .toSet();

        if (options.isEmpty()) {
            return state;
        }

        return state.appendAction(new ReturnMeepleAction(options, ReturnMeepleSource.ROBBERSSON));
    }
}
