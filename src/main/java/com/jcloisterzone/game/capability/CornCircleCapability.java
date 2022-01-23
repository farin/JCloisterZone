package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.CornCircleRemoveOrDeployMessage.CornCircleOption;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

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
	private static final CornCircleModifier CORN_CIRCLE_FIELD = new CornCircleModifier(Field.class);

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        Vector<Element> circleEl = XMLUtils.getElementStreamByTagName(tileElement, "corn-circle").toVector();
		if (circleEl.size() == 0) {
			return tile;
		}
		if (circleEl.size() == 1) {
            String type = circleEl.get().getAttribute("type");
            CornCircleModifier modifier;
            switch (type) {
            case "Road":
            	modifier = CORN_CIRCLE_ROAD;
            	break;
            case "City":
            	modifier = CORN_CIRCLE_CITY;
            	break;
            case "Field":
            	if (!state.getBooleanRule(Rule.FARMERS)) {
            		return tile;
				}
            	modifier = CORN_CIRCLE_FIELD;
            	break;
            default:
            	throw new IllegalArgumentException("Invalid corn circle type.");
            }
            return tile.addTileModifier(modifier);
        }
        throw new IllegalStateException("multiple <corn-circle> elements");
    }
}
