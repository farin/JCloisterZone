package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;

public class PigHerdCapability extends Capability {

	public PigHerdCapability(Game game) {
		super(game);
	}

	@Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof Farm) {
        	if (attributeBoolValue(xml, "pig")) {
	            if (game.getBooleanValue(CustomRule.PIG_HERD_ON_GQ_FARM) || !"GQ.F".equals(tile.getId())) {
	            	((Farm) feature).setPigHerd(true);
	            }
            }
        }
    }
}
