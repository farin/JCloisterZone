package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.*;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.feature.modifier.StringNonMergingNonEmptyModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.PlaceTile;
import io.vavr.collection.List;

import static com.jcloisterzone.XMLUtils.attributeIntValue;
import static com.jcloisterzone.XMLUtils.attributeStringValue;

import org.w3c.dom.Element;

public class FamiliesCapability extends Capability<PlacedTile> {

	public static final StringNonMergingNonEmptyModifier FAMILY = new StringNonMergingNonEmptyModifier("family", null);

	@Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City) {
        	if (attributeIntValue(xml, "pennants", 0) > 0 ) {
        		String family = "blue";
        		if (xml.hasAttribute("family")) {
        			family = xml.getAttribute("family");
        		}
        		feature = ((City) feature).putModifier(FAMILY, family);
        	}
        }
        return feature;
    }

	@Override
    public boolean isTilePlacementAllowed(GameState state, Tile tile, PlacementOption placement) {
        Position pos = placement.getPosition();
        Rotation rot = placement.getRotation();

        state = (new PlaceTile(tile, pos, rot)).apply(state);

        List<City> cities = state.getTileFeatures2(pos, Structure.class)
                .filter(fp -> City.class.isInstance(fp._2))
        		.map(t -> (City) t._2)
           		.toList();

        if (cities.size()==0) {
        	// No city
        	return true;
        }
        
        for(City city : cities) {
        	if (city.getModifier(state, City.PENNANTS, 0) > 0) {
        		if (city.getModifier(state, FAMILY, null).equals("")) {
        			// Family is null, not allowed placement, because families joined due to StringNonMergingModifier = null
        			return false;
        		}
        	}
        }
        
        return true;
    }
}
