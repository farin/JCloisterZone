package com.jcloisterzone.board;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EdgeTest {

    @Test
    public void translate() {
        assertEquals(
            new Edge(new Position(2, 3), new Position(2, 4)),
            new Edge(new Position(0, 0), new Position(0, 1))
                .translate(new Position(2, 3))
        );
    }

    @Test
    public void rotateCW() {
        assertEquals(
            new Edge(new Position(-1, 0), new Position(0, 0)),
            new Edge(new Position(0, 0), new Position(0, 1))
                .rotateCW(new Position(0, 0), Rotation.R90)
        );
        assertEquals(
            new Edge(new Position(0, 0), new Position(0, 1)),
            new Edge(new Position(0, 0), new Position(0, 1))
                .rotateCW(new Position(0, 0), Rotation.R0)
        );
        assertEquals(
            new Edge(new Position(0, 1), new Position(1, 1)),
            new Edge(new Position(0, 0), new Position(0, 1))
                .rotateCW(new Position(0, 1), Rotation.R90)
        );
    }

    @Test
    public void rotateCCW() {
        assertEquals(
            new Edge(new Position(0, 0), new Position(1, 0)),
            new Edge(new Position(0, 0), new Position(0, 1))
                .rotateCCW(new Position(0, 0), Rotation.R90)
        );
        assertEquals(
            new Edge(new Position(-1, 1), new Position(0, 1)),
            new Edge(new Position(0, 0), new Position(0, 1))
                .rotateCCW(new Position(0, 1), Rotation.R90)
        );
    }
}
