package com.jcloisterzone.game.capability;

import com.jcloisterzone.event.play.ScoreEvent.ReceivedPoints;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;

/** model contains placement of last placed rose */
public class WindRoseCapability extends Capability<PlacedTile> {

	public static class WindRoseModifier extends TileModifier {
		private Location rose;

		WindRoseModifier(Location rose) {
			super("WindRose" + rose.toString());
			this.rose = rose;
		}

		public Location getRose() {
			return rose;
		}
	}

	private static final long serialVersionUID = 1L;

    public static final int WIND_ROSE_POINTS = 3;

    public static final Map<Location, WindRoseModifier> ROSES = HashMap.of(
    	Location.NWSE, new WindRoseModifier(Location.NWSE),
    	Location.NW, new WindRoseModifier(Location.NW),
    	Location.NE, new WindRoseModifier(Location.NE),
    	Location.SW, new WindRoseModifier(Location.SW),
    	Location.SE, new WindRoseModifier(Location.SE)
    );


    @Override
    public GameState onTilePlaced(GameState state, PlacedTile pt) {
    	Location rose = pt.getTile().getTileModifiers()
    			.find(m -> m instanceof WindRoseModifier)
    			.map(m -> ((WindRoseModifier)m).getRose())
    			.getOrNull();
        if (rose == null) {
            return state;
        }
        if (rose == Location.NWSE) {
            return setModel(state, pt);
        }
        PlacedTile ptRose = getModel(state);
        rose = rose.rotateCW(ptRose.getRotation());
        if (isInProperQuadrant(rose, pt.getPosition(), ptRose.getPosition())) {
            Player p = state.getTurnPlayer();
            state = (new AddPoints(p, WIND_ROSE_POINTS)).apply(state);
            ScoreEvent scoreEvent = new ScoreEvent(new ReceivedPoints(WIND_ROSE_POINTS, null, p, pt.getPosition()), "Wind-rose", false, false);
            state = state.appendEvent(scoreEvent);
        }
        return state;
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        for (Element el : tileElements) {
            if (el.hasAttribute("wind-rose")) {
                Location loc = Location.valueOf(el.getAttribute("wind-rose"));
                tile = tile.addTileModifier(ROSES.get(loc).getOrElseThrow(IllegalArgumentException::new));
            }
        }
        return tile;
    }

    private boolean isInProperQuadrant(Location rose, Position pos, Position rosePosition) {
        if (rose == Location.NW) {
            return pos.x <= rosePosition.x && pos.y <= rosePosition.y;
        }
        if (rose == Location.NE) {
            return pos.x >= rosePosition.x && pos.y <= rosePosition.y;
        }
        if (rose == Location.SW) {
            return pos.x <= rosePosition.x && pos.y >= rosePosition.y;
        }
        if (rose == Location.SE) {
            return pos.x >= rosePosition.x && pos.y >= rosePosition.y;
        }
        throw new IllegalArgumentException("Wrong rose argument");
    }
}
