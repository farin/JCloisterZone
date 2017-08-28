package com.jcloisterzone.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class EdgePatternTest {


    @Test
    public void rotate() {
        assertEquals("RC?F", EdgePattern.fromString("RC?F").rotate(Rotation.R0).toString());
        assertEquals("FRC?", EdgePattern.fromString("RC?F").rotate(Rotation.R90).toString());
        assertEquals("?FRC", EdgePattern.fromString("RC?F").rotate(Rotation.R180).toString());
        assertEquals("C?FR", EdgePattern.fromString("RC?F").rotate(Rotation.R270).toString());
    }

    @Test
    public void equals() {
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("RC?F"));
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("FRC?"));
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("?FRC"));
        assertEquals(EdgePattern.fromString("RC?F"), EdgePattern.fromString("C?FR"));
    }

    @Test
    public void isMatchingExact() {
        assertTrue(EdgePattern.fromString("RC?F").isMatchingExact(EdgePattern.fromString("RCRF")));
        assertFalse(EdgePattern.fromString("RC?F").isMatchingExact(EdgePattern.fromString("FFRC")));
        assertTrue(EdgePattern.fromString("????").isMatchingExact(EdgePattern.fromString("IRIF")));
    }

    @Test
    public void isMatchingAnyRotation() {
        assertTrue(EdgePattern.fromString("RC?F").isMatchingAnyRotation(EdgePattern.fromString("RCRF")));
        assertTrue(EdgePattern.fromString("RC?F").isMatchingAnyRotation(EdgePattern.fromString("FFRC")));
        assertTrue(EdgePattern.fromString("????").isMatchingAnyRotation(EdgePattern.fromString("IRIF")));
    }

    @Test
    public void getSymmetry() {
        assertEquals(TileSymmetry.S4, EdgePattern.fromString("CCCC").getSymmetry());
        assertEquals(TileSymmetry.S4, EdgePattern.fromString("RRRR").getSymmetry());
        assertEquals(TileSymmetry.S2, EdgePattern.fromString("CICI").getSymmetry());
        assertEquals(TileSymmetry.NONE, EdgePattern.fromString("CIRI").getSymmetry());
        assertEquals(TileSymmetry.NONE, EdgePattern.fromString("FRCR").getSymmetry());
    }

    @Test
    public void getBridgePattern() {
        assertEquals("RFRF", EdgePattern.fromString("FFFF").getBridgePattern(Location.NS).toString());
        assertEquals("FRFR", EdgePattern.fromString("FFFF").getBridgePattern(Location.WE).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBridgePatternThrows() {
        EdgePattern.fromString("RRRF").getBridgePattern(Location.NS);
    }

}

