package com.jcloisterzone.feature;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.game.CustomRule;

public class CityScoring extends AbstractScoringTest {

    protected void assertScore(int expected, Tile tile, Location loc) {
        City city = (City) tile.getFeaturePartOf(loc);
        CityScoreContext ctx = city.getScoreContext();
        city.walk(ctx);

        assertEquals(expected, ctx.getPoints());
    }

    @Test
    public void tiny() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
        putTile(new Position(0,-1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCCC");

        assertScore(4, t, Location.N);
    }

    @Test
    public void tinyCustomRule() {
        game.getCustomRules().add(CustomRule.TINY_CITY_2_POINTS);

        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
        putTile(new Position(0,-1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCCC");

        assertScore(2, t, Location.N);
    }

    @Test
    public void simple() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
         putTile(new Position(0,-1), Rotation.R180, Expansion.BASIC, "CcRr");
        putTile(new Position(1,-1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCCC");

        assertScore(6, t, Location.N);
    }

    @Test
    public void pennant() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
         putTile(new Position(0,-1), Rotation.R180, Expansion.BASIC, "CcRr+");
        putTile(new Position(1,-1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCCC");

        assertScore(8, t, Location.N);
    }

    @Test
    public void morePennants() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "RCr");
         putTile(new Position(0,-1), Rotation.R180, Expansion.BASIC, "CcRr+");
        putTile(new Position(1,-1), Rotation.R0, Expansion.ABBEY_AND_MAYOR, "C!+");

        assertScore(10, t, Location.N);
    }

    @Test
    public void twoPennantTile() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.ABBEY_AND_MAYOR, "C++");
        putTile(new Position(-1,0), Rotation.R90, Expansion.ABBEY_AND_MAYOR, "CCRR");
        putTile(new Position(1,0), Rotation.R0, Expansion.ABBEY_AND_MAYOR, "C!+");
        putTile(new Position(0,1), Rotation.R0, Expansion.ABBEY_AND_MAYOR, "CCc+");
        putTile(new Position(0,-1), Rotation.R180, Expansion.ABBEY_AND_MAYOR, "CRr");

        assertScore(18, t, Location.N);
    }

    @Test
    public void unfinshedCathedral() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "Cccc.c");
        putTile(new Position(0, -1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCc+");

        assertScore(0, t, Location.N);
    }

    @Test
    public void finshedCathedral() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "Cccc.c");
        putTile(new Position(0,-1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCc+");
        putTile(new Position(0,1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "C!");
        putTile(new Position(1,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCC");
        putTile(new Position(-1,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CCC");

        assertScore(15, t, Location.N);
    }

    @Test
    public void twoCathedrals() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "Cccc.c");
        putTile(new Position(1,0), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "Cccc.c");
        putTile(new Position(0,-1), Rotation.R180, Expansion.INNS_AND_CATHEDRALS, "CcRr+.i");
        putTile(new Position(1,-1), Rotation.R270, Expansion.INNS_AND_CATHEDRALS, "CcRr+.i");
        putTile(new Position(0,1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CFR");
        putTile(new Position(1,1), Rotation.R0, Expansion.INNS_AND_CATHEDRALS, "CFR");
        putTile(new Position(2,0), Rotation.R270, Expansion.INNS_AND_CATHEDRALS, "CFR");
        putTile(new Position(-1,0), Rotation.R90, Expansion.INNS_AND_CATHEDRALS, "CFR");

        assertScore(30, t, Location.N);
    }

    @Test
    public void siege() {
        Tile t;
        t = putTile(new Position(0,0), Rotation.R0, Expansion.CATHARS, "C");
        putTile(new Position(0,-1), Rotation.R270, Expansion.CATHARS, "Cc");
        putTile(new Position(-1,-1), Rotation.R90, Expansion.CATHARS, "C");

        assertScore(3, t, Location.N);
    }

}
