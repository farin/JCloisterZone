package com.jcloisterzone.game.capability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.SnapshotCorruptedException;

public class WindRoseCapability extends Capability {

    public static final int WIND_ROSE_POINTS = 3;

    private Rotation roseRotation;
    private Position rosePosition;

    public WindRoseCapability(final Game game) {
        super(game);
    }

    @Override
    public void tilePlaced(Tile tile) {
        Location rose = tile.getWindRose();
        if (rose == null) return;
        if (rose == Location.NWSE) {
            roseRotation = tile.getRotation();
            rosePosition = tile.getPosition();
        } else {
            rose = rose.rotateCW(roseRotation);
            if (isInProperQuadrant(rose, tile.getPosition())) {
                Player p = game.getActivePlayer();
                p.addPoints(WIND_ROSE_POINTS, PointCategory.WIND_ROSE);
                game.fireGameEvent().scored(tile.getPosition(), p, WIND_ROSE_POINTS, WIND_ROSE_POINTS+"", false);
            }
        }
    }

    @Override
    public Object backup() {
        return new Object[] { rosePosition, roseRotation };
    }

    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        rosePosition = (Position) a[0];
        roseRotation = (Rotation) a[1];
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.hasAttribute("wind-rose")) {
            Location loc = Location.valueOf(xml.getAttribute("wind-rose"));
            tile.setWindRose(loc);
        }
    }

    private boolean isInProperQuadrant(Location rose, Position pos) {
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

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        node.setAttribute("rotation", roseRotation.name());
        XmlUtils.injectPosition(node, rosePosition);
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
        roseRotation = Rotation.valueOf(node.getAttribute("rotation"));
        rosePosition = XmlUtils.extractPosition(node);
    }

}
