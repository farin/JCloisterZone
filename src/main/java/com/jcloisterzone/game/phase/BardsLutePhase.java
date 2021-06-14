package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.BardsLuteAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.event.TokenPlacedEvent;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.BardsLuteCapability;
import com.jcloisterzone.game.capability.BardsLuteCapability.BardsLuteToken;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.Player;

import io.vavr.collection.List;
import io.vavr.collection.Set;

public class BardsLutePhase extends Phase {

    public BardsLutePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        PlacedTile placedTile = state.getLastPlaced();
        if (placedTile.getTile().hasModifier(BardsLuteCapability.BARDS_LUTE)) {
            Set<FeaturePointer> options = state.getTileFeatures2(placedTile.getPosition(), Feature.class)
                    .filter(fp -> Field.class.isInstance(fp._2) || Completable.class.isInstance(fp._2))
                    .map(fp -> fp._1)
                    .toSet();
            
            List<FeaturePointer> occupiedFeaturePointers = state.getDeployedMeeples()
            		.map(t -> t._2)
            		.toList();
            
            List<Feature> occupiedFeatures = List.empty();
            
            for(FeaturePointer ofp: occupiedFeaturePointers) {
            	occupiedFeatures = occupiedFeatures.append(state.getFeature(ofp));
            }
            occupiedFeatures = occupiedFeatures.distinct();
            for(FeaturePointer fp : options) {
            	for(Feature f: occupiedFeatures) {
            		if (f.equals(state.getFeature(fp))) {
                		options = options.remove(fp);
            			continue;
            		}
            	}
            }

            BardsLuteAction action = new BardsLuteAction(options, BardsLuteToken.BARDS_NOTE);
            state = state.setPlayerActions(new ActionsState(state.getTurnPlayer(), action, false));
            return promote(state);
        }
        return next(state);
    }

    private GameState placeBardsLuteToken(GameState state, FeaturePointer fp) {
        state = state.appendEvent(new TokenPlacedEvent(PlayEventMeta.createWithoutPlayer(), BardsLuteToken.BARDS_NOTE, fp));

        return state.getCapabilities().get(BardsLuteCapability.class).updateModel(state, fps -> {
			return fps.put(fp, true);
		});
    }

    @PhaseMessageHandler
    public StepResult handlePlaceTokenMessage(GameState state, PlaceTokenMessage msg) {
    	System.out.println(msg);
        Token token = msg.getToken();
        FeaturePointer pos = (FeaturePointer) msg.getPointer();

        if (token != BardsLuteToken.BARDS_NOTE) {
            throw new IllegalArgumentException();
        }
        state = placeBardsLuteToken(state, pos);
        state = clearActions(state);
        return next(state);
    }
}
