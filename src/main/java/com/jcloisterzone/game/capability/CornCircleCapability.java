package com.jcloisterzone.game.capability;

import com.jcloisterzone.game.Rule;
import org.w3c.dom.Element;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCircleOption;

import io.vavr.collection.Vector;

public class CornCircleCapability extends Capability<CornCircleOption> {

	public static class CornCircleModifier extends TileModifier {
		private Class<? extends Feature> featureType;

		CornCircleModifier(Class<? extends Feature> featureType) {
			super("CornCircle" + featureType.getSimpleName());
			this.featureType = featureType;
		}

		public Class<? extends Feature> getFeatureType() {
			return featureType;
		}
	}

	private static final long serialVersionUID = 1L;

	private static final CornCircleModifier CORN_CIRCLE_ROAD = new CornCircleModifier(Road.class);
	private static final CornCircleModifier CORN_CIRCLE_CITY = new CornCircleModifier(City.class);
	private static final CornCircleModifier CORN_CIRCLE_FARM = new CornCircleModifier(Farm.class);

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        Vector<Element> circleEl = XMLUtils.getElementStreamByTagName(tileElements, "corn-circle").toVector();
        assert circleEl.size() <= 1;
        for (Element el : circleEl) {
            String type = el.getAttribute("type");
            CornCircleModifier modifier;
            switch (type) {
            case "Road":
            	modifier = CORN_CIRCLE_ROAD;
            	break;
            case "City":
            	modifier = CORN_CIRCLE_CITY;
            	break;
            case "Farm":
            	if (!state.getBooleanValue(Rule.FARMERS)) {
            		return tile;
				}
            	modifier = CORN_CIRCLE_FARM;
            	break;
            default:
            	throw new IllegalArgumentException("Invalid corn cicle type.");
            }
            return tile.addTileModifier(modifier);
        }
        return tile;
    }
}
