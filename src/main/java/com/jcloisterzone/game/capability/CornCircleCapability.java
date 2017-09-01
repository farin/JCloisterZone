package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCircleOption;

public class CornCircleCapability extends Capability<CornCircleOption> {

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("corn-circle");
        if (nl.getLength() > 0) {
            String type = ((Element)nl.item(0)).getAttribute("type");
            Class<? extends Feature> cornCircleType = null;
            if ("Road".equals(type)) cornCircleType = Road.class;
            if ("City".equals(type)) cornCircleType = City.class;
            if ("Farm".equals(type)) cornCircleType = Farm.class;
            if (cornCircleType == null) throw new IllegalArgumentException("Invalid corn cicle type.");
            return tile.setCornCircle(cornCircleType);
        }
        return tile;
    }
}
