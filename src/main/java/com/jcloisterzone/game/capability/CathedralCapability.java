package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class CathedralCapability extends Capability {

    public CathedralCapability(Game game) {
        super(game);
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof City) {
            ((City) feature).setCathedral(attributeBoolValue(xml, "cathedral"));
        }
    }



}
