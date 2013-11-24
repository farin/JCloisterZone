package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XmlUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class InnCapability extends Capability {

    public InnCapability(Game game) {
        super(game);
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof Road) {
            ((Road) feature).setInn(attributeBoolValue(xml, "inn"));
        }
    }
}
