package com.jcloisterzone.board;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PositionTest {

    @Test
    public void add() {
        assertEquals(
            new Position(5, 10),
            new Position(2, 3).add(new Position(3, 7))
        );
    }

    @Test
    public void subtract() {
        assertEquals(
            new Position(-1, -4),
            new Position(2, 3).subtract(new Position(3, 7))
        );
    }

    @Test
    public void rotateCW() {
        assertEquals(new Position(2, 3), new Position(2, 3).rotateCW(Rotation.R0));
        assertEquals(new Position(-3, 2), new Position(2, 3).rotateCW(Rotation.R90));
        assertEquals(new Position(-2, -3), new Position(2, 3).rotateCW(Rotation.R180));
        assertEquals(new Position(3, -2), new Position(2, 3).rotateCW(Rotation.R270));
    }

}
