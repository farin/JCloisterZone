package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.collection.Vector;

/** model contains placement of last placed rose */
public class WindRoseCapability extends Capability<PlacedTile> {

    public static final int WIND_ROSE_POINTS = 3;

    @Override
    public GameState onTilePlaced(GameState state, PlacedTile pt) {
        Location rose = pt.getTile().getWindRose();
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
            state = (new AddPoints(p, WIND_ROSE_POINTS, PointCategory.WIND_ROSE)).apply(state);
            ScoreEvent scoreEvent = new ScoreEvent(
                WIND_ROSE_POINTS,
                WIND_ROSE_POINTS + "",
                PointCategory.WIND_ROSE,
                false,
                pt.getPosition(),
                p
            );
            state = state.appendEvent(scoreEvent);
        }
        return state;
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        for (Element el : tileElements) {
            if (el.hasAttribute("wind-rose")) {
                Location loc = Location.valueOf(el.getAttribute("wind-rose"));
                tile = tile.setWindRose(loc);
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
