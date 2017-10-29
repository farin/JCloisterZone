package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCircleOption;

import io.vavr.collection.Vector;

public class CornCircleCapability extends Capability<CornCircleOption> {

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        Vector<Element> circleEl = XMLUtils.getElementStreamByTagName(tileElements, "corn-circle").toVector();
        assert circleEl.size() <= 1;
        for (Element el : circleEl) {
            String type = el.getAttribute("type");
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
