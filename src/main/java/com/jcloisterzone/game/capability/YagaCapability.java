package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class YagaCapability extends Capability {

	public YagaCapability(Game game) {
		super(game);
	}

	@Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof Cloister) {
            ((Cloister)feature).setYagaHut(attributeBoolValue(xml, "yaga"));
        }
    }

}
