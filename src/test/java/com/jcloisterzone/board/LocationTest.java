package com.jcloisterzone.board;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LocationTest {

    @Test
    public void isPartOf() {
        assertTrue(Location.E.isPartOf(Location.E));
        assertTrue(Location.N.isPartOf(Location.NW));
        assertFalse(Location.E.isPartOf(Location.NW));
        assertFalse(Location.INNER_FIELD.isPartOf(Location.N));
        assertTrue(Location._N.isPartOf(Location.NWSE));
    }

    @Test
    public void union() {
        assertEquals(Location.SW, Location.S.union(Location.W));
        assertEquals(Location.SW, Location.W.union(Location.S));
        assertEquals(Location.NWSE, Location.N.union(Location._N));

    }

    @Test
    public void unionExcept() {
        try {
            Location.N.union(Location.MONASTERY);
            fail();
        } catch (IllegalArgumentException ae) {
        }
        try {
            Location.N.union(Location.INNER_FIELD);
            fail();
        } catch (IllegalArgumentException ae) {
        }
    }

    @Test
    public void subtract() {
        assertEquals(Location.S, Location.SW.subtract(Location.W));
        assertEquals(Location.W, Location.SW.subtract(Location.S));
        assertEquals(Location.N, Location.NWSE.subtract(Location._N));
    }

    @Test()
    public void subtractExcept() {
        try {
            Location.N.subtract(Location.MONASTERY);
            fail();
        } catch (IllegalArgumentException ae) {
        }
    }

    @Test
    public void rev() {
        assertEquals(Location.S, Location.N.rev());
        assertEquals(Location.NL, Location.SR.rev());
        assertEquals(Location.NW, Location.SE.rev());
        assertEquals(Location._N, Location._S.rev());
        Location l1 = Location.NL.union(Location.NR.union(Location.EL));
        Location l2 = Location.SL.union(Location.SR.union(Location.WR));
        assertEquals(l1, l2.rev());
    }

    @Test
    public void rotate() {
        assertEquals(Location.E, Location.N.rotateCW(Rotation.R90));
        assertEquals(Location.W, Location.N.rotateCCW(Rotation.R90));
        assertEquals(Location.W, Location.E.rotateCW(Rotation.R180));
        assertEquals(Location.S, Location.S.rotateCCW(Rotation.R0));
    }

    @Test
    public void isRotationOf() {
        assertTrue(Location.E.isRotationOf(Location.E));
        assertTrue(Location.E.isRotationOf(Location.N));
        assertTrue(Location.N.isRotationOf(Location.E));
        assertTrue(Location._N.isRotationOf(Location._S));
        assertTrue(Location.NW.isRotationOf(Location.NE));
    }

    @Test
    public void getRotationOf() {
        assertEquals(Rotation.R0, Location.E.getRotationOf(Location.E));
        assertEquals(Rotation.R90, Location.E.getRotationOf(Location.N));
        assertEquals(Rotation.R270, Location.S.getRotationOf(Location.W));
    }

    @Test
    public void intersect() {
        assertEquals(Location.E, Location.E.intersect(Location.E));
        assertNull(Location.E.intersect(Location.W));
        assertEquals(Location.E, Location.WE.intersect(Location.SE));
        assertNull(Location.NW.intersect(Location.NR.union(Location.EL)));
    }

    @Test
    public void fieldToSide() {
        assertEquals(Location.E, Location.EL.fieldToSide());
        assertEquals(Location.E, Location.ER.fieldToSide());
        assertEquals(Location.E, Location.EL.union(Location.ER).fieldToSide());
        assertEquals(Location.NW, Location.NL.union(Location.WR).fieldToSide());
    }
}


