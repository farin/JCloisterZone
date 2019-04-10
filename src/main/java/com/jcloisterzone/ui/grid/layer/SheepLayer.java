package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.capability.SheepCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class SheepLayer extends AbstractGridLayer {

	private final static Color FILL_COLOR = new Color(76,170,13,140);

	private Map<Tuple2<FeaturePointer, ImmutablePoint>, Integer> placedTokens = HashMap.empty();

	public SheepLayer(GridPanel gridPanel, GameController gc) {
		super(gridPanel, gc);
	}

	@Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
		// TODO nice to have, recalculate only when placed tokens changed
		GameState state = ev.getCurrentState();
        placedTokens = state.getCapabilityModel(SheepCapability.class)
        		.mapKeys(fp -> {
        			PlacedTile tile = state.getPlacedTile(fp.getPosition());
        			ImmutablePoint point = rm.getMeeplePlacement(tile.getTile(), tile.getRotation(), fp.getLocation());
        			return new Tuple2<>(fp, point.translate(20, -5)); // shift circle out of shepherd
        		})
        		.mapValues(tokens -> tokens.foldLeft(0, (acc, token) -> acc + token.sheepCount()));
    }

	@Override
	public void paint(Graphics2D g2) {
		int width = getTileWidth();
		int height = getTileHeight();
        int rx = (int)(width*0.15);
        int ry = (int)(height*0.15);

        g2.setColor(FILL_COLOR);
        placedTokens.forEach((t, count) -> {
        	FeaturePointer fp = t._1;
        	ImmutablePoint point = t._2;

            AffineTransform at = getAffineTransformIgnoringRotation(fp.getPosition());
            int x = (int)(point.getX() / 100.0 * width);
            int y = (int)(point.getY() / 100.0 * height);
            Ellipse2D shape = new Ellipse2D.Double(x - rx, y - ry, 2 * rx, 2 * ry);
            g2.fill(at.createTransformedShape(shape));

            ImmutablePoint rotatedPoint = point.rotate100(gridPanel.getBoardRotation().inverse());
            drawAntialiasedTextCentered(g2, ""+count, 20, fp.getPosition(), rotatedPoint, Color.WHITE, null);
        });

	}

}
