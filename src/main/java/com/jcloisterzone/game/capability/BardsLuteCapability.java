package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PlaceTile;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

/**
 * Capability model is {@code HashMap<FeaturePointer,Boolean>} - HashMap of FeaturePointer of placed Notes and visible or not
 */
public class BardsLuteCapability extends Capability<HashMap<FeaturePointer,Boolean>> {

	public enum BardsLuteToken implements Token {
		BARDS_NOTE
    }

	public static final TileModifier BARDS_LUTE = new TileModifier("BardsLute");

    @Override
    public GameState onStartGame(GameState state) {
        state = setModel(state, HashMap.empty());
        return state;
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        if (tileElement.hasAttribute("bards-lute")) {
            tile = tile.addTileModifier(BARDS_LUTE);
        }

        return tile;
    }

    @Override
    public boolean isTilePlacementAllowed(GameState state, Tile tile, PlacementOption placement) {
        if (!tile.hasModifier(BARDS_LUTE)) {
            return true;
        }

        Position pos = placement.getPosition();
        Rotation rot = placement.getRotation();

        state = (new PlaceTile(tile, pos, rot)).apply(state);
        
        List<FeaturePointer> fps = state.getTileFeatures2(pos, Structure.class)
                .filter(fp -> Field.class.isInstance(fp._2) || Completable.class.isInstance(fp._2))
        		.map(t -> t._1)
           		.toList();

        if (!state.getBooleanRule(Rule.FARMERS)) {
        	fps = fps.filter(fp -> !(Field.class.isInstance(fp)));
        }

        List<FeaturePointer> occupiedFeaturePointers = state.getDeployedMeeples()
        		.map(t -> t._2)
        		.toList();
        
        List<Feature> occupiedFeatures = List.empty();
        
        for(FeaturePointer ofp: occupiedFeaturePointers) {
        	occupiedFeatures = occupiedFeatures.append(state.getFeature(ofp));
        }
        occupiedFeatures = occupiedFeatures.distinct();
        Integer occupied = 0;
        for(FeaturePointer fp : fps) {
        	for(Feature f: occupiedFeatures) {
        		if (f.equals(state.getFeature(fp))) {
            		occupied++;
        			continue;
        		}
        	}
        }
        return fps.size()>occupied;
    }
    
    public static int getPlacedTokensCount(GameState state, List<FeaturePointer> places) {
    	return places.toSet().intersect(state.getCapabilityModel(BardsLuteCapability.class)
    			.filter(t -> t._2)
    			.map(t -> t._1)
    			.toSet()).size();
    }
    
    public GameState removePlacedToken(GameState state, List<FeaturePointer> places) {
        return updateModel(state, model -> {
        	return model.map((k,v) -> Tuple.of(k, places.contains(k) ? false : v));
        });
    }
}
