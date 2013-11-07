package com.jcloisterzone.feature;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.visitor.score.RoadScoreContext;

public class RoadScoring extends AbstractScoringTest {

    protected void assertScore(int expected, Tile tile, Location loc) {
        Road road = (Road) tile.getFeaturePartOf(Location.W);
        RoadScoreContext ctx = road.getScoreContext();
        road.walk(ctx);

        assertEquals(expected, ctx.getPoints());
    }

    @Test
    public void simple() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
        putTile(new Position(1,0), Rotation.R0, Expansion.BASIC, "RFr");
        putTile(new Position(-1,0), Rotation.R0, Expansion.BASIC, "RRRR");
        putTile(new Position(2,0), Rotation.R90, Expansion.BASIC, "LR");

        assertScore(4, t, Location.W);
    }

    @Test
    public void circle() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "Rr");
        putTile(new Position(-1,0), Rotation.R270, Expansion.BASIC, "Rr");
        putTile(new Position(0,1), Rotation.R90, Expansion.BASIC, "RrC");
        putTile(new Position(-1,1), Rotation.R270, Expansion.BASIC, "CcRr");

        assertScore(4, t, Location.W);
    }

    @Test
    public void circleWithCross() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "Rr");
        putTile(new Position(-1,0), Rotation.R270, Expansion.BASIC, "Rr");
        putTile(new Position(0,1), Rotation.R90, Expansion.BASIC, "RrC");
        putTile(new Position(-1,1), Rotation.R0, Expansion.BASIC, "RRRR");

        assertScore(4, t, Location.W);
    }

    @Test
    public void inn() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
        putTile(new Position(-1,0), Rotation.R0, Expansion.BASIC, "RRRR");
        putTile(new Position(1,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "RFr.i");
        putTile(new Position(2,0), Rotation.R90, Expansion.BASIC, "LR");

        assertScore(8, t, Location.W);
    }

    @Test
    public void unfinishedInn() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
        putTile(new Position(-1,0), Rotation.R0, Expansion.BASIC, "RRRR");
        putTile(new Position(1,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "RFr.i");

        assertScore(0, t, Location.W);
    }

    @Test
    public void well() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.ABBEY_AND_MAYOR, "Rrr");
        putTile(new Position(0,-1), Rotation.R0, Expansion.ABBEY_AND_MAYOR, "R");
        putTile(new Position(-1, 0), Rotation.R270, Expansion.ABBEY_AND_MAYOR, "R");
        putTile(new Position(1, 0), Rotation.R90, Expansion.ABBEY_AND_MAYOR, "R");

        assertScore(4, t, Location.W);
    }

}
